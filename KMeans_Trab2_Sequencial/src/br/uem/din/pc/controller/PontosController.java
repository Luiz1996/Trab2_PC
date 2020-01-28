/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uem.din.pc.controller;

import static br.uem.din.pc.main.Main.houveAlteracao;
import java.util.List;
import br.uem.din.pc.model.PontosModel;
import br.uem.din.pc.model.CoordenadasModel;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Luiz Flávio
 */
public class PontosController {
    
    public static Scanner in = new Scanner(System.in);

    public static void calculaKMeansSequencial(PontosModel dadosBase, PontosModel dadosCentroit) throws Exception {
        if (!dadosBase.getPontos().isEmpty() && !dadosCentroit.getPontos().isEmpty()) {
            houveAlteracao = true;
            System.out.println("\nOs dados estao sendo processados, aguarde um momento...\n");

            int qtdePontosBase = dadosBase.getPontos().size();
            int qtdePontosCentroide = dadosCentroit.getPontos().size();
            int qtdeCoordenadas = dadosCentroit.getPontos().get(0).getCoordenada().size();

            while (houveAlteracao) {
                atualizarVinculoPontoECentroide(dadosBase, dadosCentroit, qtdePontosBase, qtdePontosCentroide);
                houveAlteracao = atualizarPosicaoCentroide(dadosBase, dadosCentroit, qtdePontosBase, qtdePontosCentroide, qtdeCoordenadas);
            }
        } else {
            System.out.println("Erro ao processar, é necessário realizar a importação do(s) arquivo(s) de base(s) e/ou centróide(s).");
        }
    }

    public static void atualizarVinculoPontoECentroide(PontosModel dadosBase, PontosModel dadosCentroit, int qtdePontosBase, int qtdePontosCentroide) throws Exception {
        for (int i = 0; i < qtdePontosBase; i++) {
            for (int j = 0; j < qtdePontosCentroide; j++) {
                double vlrDistanciaPontoBase_PontoCentroide = Math.floor(calcularDistanciaEntrePontoBase_PontoCentroide(dadosBase.getPontos().get(i), dadosCentroit.getPontos().get(j)));
                if (vlrDistanciaPontoBase_PontoCentroide < dadosBase.getPontos().get(i).getDistanciaCentroid()) {
                    
                    //se identificado uma distancia menor, então é atualizado o vinculo entre ponto base x ponto centróide
                    dadosBase.getPontos().get(i).setDistanciaCentroid(vlrDistanciaPontoBase_PontoCentroide);
                    dadosBase.getPontos().get(i).setIdCentroide((j + 1));
                }
            }
        }
    }

    public static double calcularDistanciaEntrePontoBase_PontoCentroide(CoordenadasModel pontoBase, CoordenadasModel pontoCentroide) throws Exception {
        double totalSum = 0;
        int qtdeCoordenadas = pontoBase.getCoordenada().size();

        for (int i = 0; i < qtdeCoordenadas; i++) {
            totalSum += Math.pow((pontoBase.getCoordenada().get(i) - pontoCentroide.getCoordenada().get(i)), 2);
        }

        return Math.sqrt(totalSum);
    }

    public static boolean atualizarPosicaoCentroide(PontosModel dadosBase, PontosModel dadosCentroit, int qtdePontosBase, int qtdePontosCentroide, int qtdeCoordenadas) {
        boolean houveAlteracao = false;

        for (int i = 0; i < qtdePontosCentroide; i++) {
            //inicializando lista auxiliar
            List<Integer> coordAcumulada = new ArrayList<>();
            for (int z = 0; z < qtdeCoordenadas; z++) {
                coordAcumulada.add(0);
            }

            int qtdePontos = 0;
            //percorre todos os pontos encontrando os vinculos, se identificar é somado as coordenadas(abaixo elas serão divididas pela quantidade de pontos encontrados para se obter a média correta)
            for (int j = 0; j < qtdePontosBase; j++) {
                if ((i + 1) == dadosBase.getPontos().get(j).getIdCentroide()) {
                    qtdePontos++;
                    for (int k = 0; k < qtdeCoordenadas; k++) {
                        coordAcumulada.set(k, (coordAcumulada.get(k) + dadosBase.getPontos().get(j).getCoordenada().get(k)));
                    }
                    dadosBase.getPontos().get(j).setDistanciaCentroid(Double.MAX_VALUE);
                }
            }

            if (qtdePontos > 0) {
                //realiza o calculo das novas médias, esse dado fica salvo no coordenadaAcumulada
                for (int j = 0; j < qtdeCoordenadas; j++) {
                    try {
                        coordAcumulada.set(j, Math.floorDiv(coordAcumulada.get(j), qtdePontos));
                    } catch (ArithmeticException e) {
                        coordAcumulada.set(j, 0);
                    }
                }

                //resetando valor de variável contadora
                qtdePontos = 0;

                //compara coordenada x coordenadaAcumulada
                if (!coordAcumulada.equals(dadosCentroit.getPontos().get(i).getCoordenada())) {
                    dadosCentroit.getPontos().get(i).setCoordenada(coordAcumulada);
                    houveAlteracao = true;
                }
            }
        }
        return houveAlteracao;
    }
    
    public static void imprimirPontos(PontosModel pontos) {
        if (pontos.getPontos().isEmpty()) {
            System.out.println("Nenhuma informação a ser impressa...");
        } else {
            for (int i = 0; i < pontos.getPontos().size(); i++) {
                System.out.print("Ponto " + (i + 1));

                if (pontos.getPontos().get(i).getIdCentroide() != -1) {
                    System.out.println(" -> [Centróide Vinculado: " + pontos.getPontos().get(i).getIdCentroide() + ", Distância: " + pontos.getPontos().get(i).getDistanciaCentroid() + "]");
                } else {
                    System.out.println("");
                }

                System.out.println(pontos.getPontos().get(i).getCoordenada().toString() + "\n");
            }
        }
    }
}
