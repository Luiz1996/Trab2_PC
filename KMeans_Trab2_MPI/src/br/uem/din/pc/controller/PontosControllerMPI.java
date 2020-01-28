/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uem.din.pc.controller;

import static br.uem.din.pc.main.Main.PROCESSO_MESTRE;
import static br.uem.din.pc.main.Main.dadosArqBase;
import static br.uem.din.pc.main.Main.dadosArqCentroide;
import static br.uem.din.pc.main.Main.houveAlteracao;
import br.uem.din.pc.model.CoordenadasModel;
import mpi.MPI;

/**
 *
 * @author Luiz Flávio
 */
public class PontosControllerMPI {

    public static void calcularKMeansMPI() throws InterruptedException, Exception {
        if (!dadosArqBase.getPontos().isEmpty() && !dadosArqCentroide.getPontos().isEmpty()) {

            int qtdePontosBase = dadosArqBase.getPontos().size();
            int qtdePontosCentroide = dadosArqCentroide.getPontos().size();

            while (houveAlteracao > 0) {
                //barreira para os processos esperarem
                MPI.COMM_WORLD.Barrier();

                //setando houveAlteracao = false para manutenção do while
                houveAlteracao = 0;

                //calculando distância entre os pontos e centroides e enviando informações com Send/MPI
                if (MPI.COMM_WORLD.Rank() != PROCESSO_MESTRE) {

                    //vetor auxiliar na utilização do MPI
                    int[] sendBuffer_IdCentroide = new int[(qtdePontosBase + 1)];

                    //realizando as chamadas do método de calculo das distancias e setando informações nas variaveis auxiliares
                    for (int i = (MPI.COMM_WORLD.Rank() - 1); i < qtdePontosBase; i += (MPI.COMM_WORLD.Size() - 1)) {
                        calcularDistanciaPontoBaseECentroide(qtdePontosCentroide, i);
                        sendBuffer_IdCentroide[i] = dadosArqBase.getPontos().get(i).getIdCentroide();
                    }

                    //setando o Rank que está enviando essas informações para uso futuro
                    sendBuffer_IdCentroide[(sendBuffer_IdCentroide.length - 1)] = (MPI.COMM_WORLD.Rank() - 1);

                    //enviando o IdCentroide mais próximo ao pontoAtual e também a distância entre ambos, essas informações serão recebidas pelo PROCESSO_MESTRE
                    MPI.COMM_WORLD.Send(sendBuffer_IdCentroide, 0, (qtdePontosBase + 1), MPI.INT, PROCESSO_MESTRE, 2);
                }

                if (MPI.COMM_WORLD.Rank() == PROCESSO_MESTRE) {
                    for (int paraCadaProcessoCervo = 1; paraCadaProcessoCervo < MPI.COMM_WORLD.Size(); paraCadaProcessoCervo++) {

                        //vetor auxiliar na utilização do MPI
                        int[] recvBuffer_IdCentroide = new int[(qtdePontosBase + 1)];

                        //recebendo as informações enviadas pelos processos cervos
                        MPI.COMM_WORLD.Recv(recvBuffer_IdCentroide, 0, (qtdePontosBase + 1), MPI.INT, MPI.ANY_SOURCE, 2);

                        int rankProcessoEnviou = recvBuffer_IdCentroide[(recvBuffer_IdCentroide.length - 1)];

                        for (int i = rankProcessoEnviou; i < qtdePontosBase; i += (MPI.COMM_WORLD.Size() - 1)) {
                            dadosArqBase.getPontos().get(i).setIdCentroide(recvBuffer_IdCentroide[i]);
                        }
                    }
                }

                //barreira para os processos esperarem
                MPI.COMM_WORLD.Barrier();

                //enviando as informações atualizadas no PROCESSO_MESTRE referente aos vinculos do PontosBase <-> PontosCentroides
                enviarDadosAtualizados_IdCentroide_E_DistanciaPontoCentroide();

                //barreira para os processos esperarem
                MPI.COMM_WORLD.Barrier();

                if (MPI.COMM_WORLD.Rank() != PROCESSO_MESTRE) {
                    for (int i = (MPI.COMM_WORLD.Rank() - 1); i < qtdePontosCentroide; i += (MPI.COMM_WORLD.Size() - 1)) {
                        calcularNovasCoordenadasDosCentroides(i);
                    }
                }

                if (MPI.COMM_WORLD.Rank() == PROCESSO_MESTRE) {
                    for (int idProcesso = 1; idProcesso < MPI.COMM_WORLD.Size(); idProcesso++) {
                        for (int i = (idProcesso - 1); i < qtdePontosCentroide; i += (MPI.COMM_WORLD.Size() - 1)) {
                            atualizarCoordenadasDosCentroides();
                        }
                    }
                }

                //atualizar a flag houveAlteracao nos cervos
                atualizarFlagHouveAlteracao();

                //enviando as informações atualizadas no PROCESSO_MESTRE referente aos deslocamentos dos centróides
                if (houveAlteracao > 0) {
                    enviarDadosAtualizados_CoordenadasCentroides();
                }
            }
        } else {
            System.out.println("Erro ao processar, é necessário realizar a importação do(s) arquivo(s) de base(s) e/ou centróide(s).");
        }
    }

    public static void calcularDistanciaPontoBaseECentroide(int qtdePontosCentroide, int pontoAtual) throws Exception {
        dadosArqBase.getPontos().get(pontoAtual).setDistanciaCentroid(Integer.MAX_VALUE);

        //calculando a distancia entre o pontoAtual e todos os demais centróides
        for (int centroideAtual = 0; centroideAtual < qtdePontosCentroide; centroideAtual++) {
            int vlrDistanciaPontoBase_PontoCentroide = Math.abs(obterDistanciaEntrePontoBase_PontoCentroide(dadosArqBase.getPontos().get(pontoAtual), dadosArqCentroide.getPontos().get(centroideAtual)));

            if (vlrDistanciaPontoBase_PontoCentroide < dadosArqBase.getPontos().get(pontoAtual).getDistanciaCentroid()) {
                dadosArqBase.getPontos().get(pontoAtual).setIdCentroide((centroideAtual + 1));
                dadosArqBase.getPontos().get(pontoAtual).setDistanciaCentroid(vlrDistanciaPontoBase_PontoCentroide);
            }
        }
    }

    public static int obterDistanciaEntrePontoBase_PontoCentroide(CoordenadasModel pontoBase, CoordenadasModel pontoCentroide) throws Exception {
        int totalSum = 0;
        int qtdeCoordenadas = pontoBase.getCoordenada().size();

        for (int i = 0; i < qtdeCoordenadas; i++) {
            totalSum += Math.pow((pontoBase.getCoordenada().get(i) - pontoCentroide.getCoordenada().get(i)), 2);
        }
        return ((int) Math.abs(Math.sqrt(totalSum)));
    }

    private static void enviarDadosAtualizados_IdCentroide_E_DistanciaPontoCentroide() throws InterruptedException {
        int qtdePontosBase = dadosArqBase.getPontos().size();

        //vetor auxiliar durante processo de BroadCast
        int[] sendIdCentroidAtualizado = new int[qtdePontosBase];

        if (MPI.COMM_WORLD.Rank() == PROCESSO_MESTRE) {
            for (int pontoAtual = 0; pontoAtual < qtdePontosBase; pontoAtual++) {
                sendIdCentroidAtualizado[pontoAtual] = dadosArqBase.getPontos().get(pontoAtual).getIdCentroide();
            }
        }

        //enviando por Broadcast as informações atualizadas no PROCESSO_MESTRE referente aos vinculos do PontosBase <-> PontosCentroides
        MPI.COMM_WORLD.Bcast(sendIdCentroidAtualizado, 0, qtdePontosBase, MPI.INT, PROCESSO_MESTRE);

        if (MPI.COMM_WORLD.Rank() != PROCESSO_MESTRE) {
            for (int pontoAtual = 0; pontoAtual < qtdePontosBase; pontoAtual++) {
                dadosArqBase.getPontos().get(pontoAtual).setIdCentroide(sendIdCentroidAtualizado[pontoAtual]);
            }
        }
    }

    private static void calcularNovasCoordenadasDosCentroides(int pontoCentroideAtual) throws InterruptedException {
        //variáveis auxiliares no processo de recalcular as coordenada de cada centroide
        int qtdePontosBase = dadosArqBase.getPontos().size();
        int qtdeCoordenadas = dadosArqCentroide.getPontos().get(0).getCoordenada().size();
        int[] coordAcumulada = new int[(qtdeCoordenadas + 2)];

        //variável responsável por obter a quantidade de pontoBaseAtual vinculados ao pontoCentroideAtual
        int qtdePontosBaseVinculadosAoCentroideAtual = 0;

        //percorre todos os pontos encontrando os vinculos, se identificar é somado as coordenadas(abaixo elas serão divididas pela quantidade de pontos encontrados)
        for (int pontoBaseAtual = 0; pontoBaseAtual < qtdePontosBase; pontoBaseAtual++) {

            //se encontrado os mesmo IdCentroide no pontoCentroideAtual e no pontoBaseAtual, então vai acumulando as coordenadas para se tirar as médias posteriormente
            if ((pontoCentroideAtual + 1) == dadosArqBase.getPontos().get(pontoBaseAtual).getIdCentroide()) {

                //incrementando contador de vinculos
                qtdePontosBaseVinculadosAoCentroideAtual++;

                //somando cada coordenada do pontoBaseAtual na variável auxiliar coordAcumulada
                for (int k = 0; k < qtdeCoordenadas; k++) {
                    coordAcumulada[k] = (coordAcumulada[k] + dadosArqBase.getPontos().get(pontoBaseAtual).getCoordenada().get(k));
                }
            }
        }

        if (qtdePontosBaseVinculadosAoCentroideAtual > 0) {
            //realiza o calculo das novas médias, esse dado fica salvo no coordAcumulada
            for (int j = 0; j < qtdeCoordenadas; j++) {

                //tirando a média das coordenadas, seder algum erro na divisão é atribuido valor zero para tratamento de exceção
                try {
                    coordAcumulada[j] = Math.floorDiv(coordAcumulada[j], qtdePontosBaseVinculadosAoCentroideAtual);
                } catch (ArithmeticException e) {
                    System.out.println("coordAcumulada[j] = " + coordAcumulada[j]);
                    System.out.println("qtdePontosBaseVinculadosAoCentroideAtual = " + qtdePontosBaseVinculadosAoCentroideAtual);
                    System.out.println(e.getMessage().trim());
                    coordAcumulada[j] = 0;
                }

                //como houve alteração na coordenada, então setamos a variável houveAlteração para 1, para que o while principal continue executando
                if (coordAcumulada[j] != dadosArqCentroide.getPontos().get(pontoCentroideAtual).getCoordenada().get(j)) {
                    houveAlteracao = 1;
                }
            }
        }

        //resetando valor de variável contadora de vinculos
        qtdePontosBaseVinculadosAoCentroideAtual = 0;

        //vetor auxiliar na utilização do MPI
        int[] sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes = new int[(qtdeCoordenadas + 2)];

        //setando novas informações
        sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes = coordAcumulada;
        sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes[qtdeCoordenadas] = pontoCentroideAtual;
        sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes[qtdeCoordenadas + 1] = houveAlteracao;

        //enviando dados para processo mestre
        MPI.COMM_WORLD.Send(sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes, 0, (qtdeCoordenadas + 2), MPI.INT, PROCESSO_MESTRE, 99);
    }

    private static void atualizarCoordenadasDosCentroides() {
        int qtdeCoordenadas = dadosArqCentroide.getPontos().get(0).getCoordenada().size();

        //vetor auxiliar na utilização do MPI
        int[] sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes = new int[(qtdeCoordenadas + 2)];

        //recebendo as informações enviadas
        MPI.COMM_WORLD.Recv(sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes, 0, (qtdeCoordenadas + 2), MPI.INT, MPI.ANY_SOURCE, 99);

        //setando as novas informações 
        for (int paraCadaCoordenadaDoCentroide = 0; paraCadaCoordenadaDoCentroide < qtdeCoordenadas; paraCadaCoordenadaDoCentroide++) {
            dadosArqCentroide.getPontos().get(sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes[qtdeCoordenadas]).getCoordenada().set(paraCadaCoordenadaDoCentroide, sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes[paraCadaCoordenadaDoCentroide]);
        }

        //basta que um cervo tenha enviado true, assim o PROCESSO_MESTRE ficará como true também
        if (sendBuffer_CoordenadasAtualizadas_IdCentroide_FlagHouveAlteracoes[(qtdeCoordenadas + 1)] > 0) {
            houveAlteracao = 1;
        }
    }

    private static void atualizarFlagHouveAlteracao() {
        //vetor auxiliar para uso em trocas de mensagens no MPI/Java
        int[] broadCastHouveAlteracao = new int[1];

        //atualizando flag houveAlteracao para os cervos
        if (MPI.COMM_WORLD.Rank() == PROCESSO_MESTRE) {
            broadCastHouveAlteracao[0] = houveAlteracao;
        }

        //enviando para todos os processos cervos a informação atualizada
        MPI.COMM_WORLD.Bcast(broadCastHouveAlteracao, 0, 1, MPI.INT, PROCESSO_MESTRE);

        //setando a nova flag nos processos cervos
        if (MPI.COMM_WORLD.Rank() != PROCESSO_MESTRE) {
            houveAlteracao = broadCastHouveAlteracao[0];
        }
    }

    private static void enviarDadosAtualizados_CoordenadasCentroides() throws InterruptedException {
        int qtdePontosCentroide = dadosArqCentroide.getPontos().size();
        int qtdeCoordenadasCentroide = dadosArqCentroide.getPontos().get(0).getCoordenada().size();

        for (int pontoAtualCentroide = 0; pontoAtualCentroide < qtdePontosCentroide; pontoAtualCentroide++) {
            //vetor auxiliar para uso em trocas de mensagens no MPI/Java
            int[] sendTodasAsCoordenadasAtualizadas = new int[qtdeCoordenadasCentroide];

            //obtendo cada coordenada dos pontos do centroide do PROCESSO_MESTRE e armazenando em vetor auxiliar
            if (MPI.COMM_WORLD.Rank() == PROCESSO_MESTRE) {
                for (int i = 0; i < qtdeCoordenadasCentroide; i++) {
                    sendTodasAsCoordenadasAtualizadas[i] = dadosArqCentroide.getPontos().get(pontoAtualCentroide).getCoordenada().get(i);
                }
            }

            //enviando por Broadcast todas as coordenadas de uma só vez
            MPI.COMM_WORLD.Bcast(sendTodasAsCoordenadasAtualizadas, 0, qtdeCoordenadasCentroide, MPI.INT, PROCESSO_MESTRE);

            //setando as informações enviadas pelo PROCESSO_MESTRE, de modo a atualizar os cervos
            if (MPI.COMM_WORLD.Rank() != PROCESSO_MESTRE) {
                for (int i = 0; i < qtdeCoordenadasCentroide; i++) {
                    dadosArqCentroide.getPontos().get(pontoAtualCentroide).getCoordenada().set(i, sendTodasAsCoordenadasAtualizadas[i]);
                }
            }
        }
    }
}
