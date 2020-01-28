/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uem.din.pc.controller;

import static br.uem.din.pc.main.Main.dadosArqBase;
import static br.uem.din.pc.main.Main.dadosArqCentroide;
import br.uem.din.pc.model.CoordenadasModel;
import br.uem.din.pc.model.PontosModel;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Luiz Flávio
 */
public class ArquivoController {

    public static void lerArquivos(int linhaASerLida/*, int imprimeDados*/) throws FileNotFoundException, IOException {

        /* 
         * linhaASerLida = 1; então serão carregados a base/centróide de tamanho 59
         * linhaASerLida = 2; então serão carregados a base/centróide de tamanho 161
         * linhaASerLida = 3; então serão carregados a base/centróide de tamanho 256
         * linhaASerLida = 4; então serão carregados a base/centróide de tamanho 1380
         * linhaASerLida = 5; então serão carregados a base/centróide de tamanho 1601
         */
        BufferedReader br = new BufferedReader(new FileReader("C:\\bases_programacao_concorrente\\busca_files.data"));

        String leitoraArquivo = null;
        for (int i = 1; i <= linhaASerLida; i++) {
            leitoraArquivo = br.readLine();

            if (i == linhaASerLida) {
                String[] caminhoArquivos = leitoraArquivo.split(",");

                //Chama método responsável por ler os arquivos e inicializar os dados da base e centróide
                dadosArqBase.setPontos(iniciarlizarDados(caminhoArquivos[0]));
                dadosArqCentroide.setPontos(iniciarlizarDados(caminhoArquivos[1]));

                //listando diretório e nome do arquivo a ser lido
                System.out.println("O arquivo ".concat(caminhoArquivos[0].concat(" foi lido com sucesso!")));
                System.out.println("O arquivo ".concat(caminhoArquivos[1].concat(" foi lido com sucesso!")));
            } else {
                leitoraArquivo = null;
            }
        }
    }

    public static List<CoordenadasModel> iniciarlizarDados(String caminhoArquivo) {
        List<CoordenadasModel> pontosTemp = new ArrayList<>();
        CoordenadasModel coordAux = new CoordenadasModel();

        //realizando leitura do arquivo
        try (
                BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha = br.readLine();
            String[] valoresLinha = null;

            //realizando a leitura de cada linha do arquivo
            while (linha != null) {
                //obtendo dados, quebrando por vígurla e salvando em vetor temporário
                valoresLinha = linha.split(",");

                //adicionando cada valor inteiro como uma 'Coordenada' do ponto que está sendo lido
                for (String vlrsDaLinha : valoresLinha) {
                    Integer vlrInt = Integer.parseInt(vlrsDaLinha);
                    coordAux.getCoordenada().add(vlrInt);
                }

                //adicionando coordenadas/ponto ao objeto a ser retornando e lendo outra linha do arquivo
                pontosTemp.add(coordAux);
                coordAux = new CoordenadasModel();
                linha = br.readLine();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Falha ao ler arquivo!\nPrograma Abortado\n\nErro: ".concat(e.getMessage().trim()), "Falha ao ler arquivo", JOptionPane.ERROR_MESSAGE);
            System.gc();
            System.exit(0);
        }
        return pontosTemp;
    }

    public static void imprimirPontos(PontosModel pontos, boolean ehDadosDaBase) {
        if (pontos.getPontos().isEmpty()) {
            System.out.println("Nenhuma informacao a ser impressa...");
        } else {
            //quando é base, apresenta-se a distancia calculada e centroide vinculado
            if (ehDadosDaBase) {
                //percorrendo todos os pontos da base
                for (int i = 0; i < pontos.getPontos().size(); i++) {
                    System.out.print(" \nPonto " + (i + 1) + " -> [Distancia: " + pontos.getPontos().get(i).getDistanciaCentroid() + "]");

                    //if (pontos.getPontos().get(i).getIdCentroide() != -1) {
                    System.out.println(" -> [Centroide Vinculado: " + pontos.getPontos().get(i).getIdCentroide() + "]");
                    /*} else {
                        System.out.println("");
                    }*/

                    System.out.println(pontos.getPontos().get(i).getCoordenada().toString().replace(" ", "").trim() + "\n");
                }
            } else {
                //quando é centroide, mostra-se apenas os pontos(não é apresentado a distancia e nem o vínculo com o ponto da base)
                //percorrendo todos os pontos do centroide
                for (int i = 0; i < pontos.getPontos().size(); i++) {
                    System.out.print(" \nPonto " + (i + 1) + "\n");
                    System.out.println(pontos.getPontos().get(i).getCoordenada().toString().replace(" ", "").trim() + "\n");
                }
            }

        }
    }

    public static void escreverArquivoCentroideAtualizado(PontosModel dadosCentroit) throws IOException {
        StringBuilder textoArquivo = new StringBuilder();
        String coordenadasAux;
        JFileChooser selecArq = new JFileChooser();
        selecArq.setFileFilter(new FileNameExtensionFilter("Data (*.data)", "data"));

        String caminhoArquivo = "";
        if (selecArq.showDialog(null, "Salvar") == JFileChooser.APPROVE_OPTION) {
            caminhoArquivo = (selecArq.getSelectedFile().getPath()).trim();
            caminhoArquivo = caminhoArquivo.replaceAll(".data", "");
            caminhoArquivo = caminhoArquivo.concat(".data");
        } else {
            System.out.println(" \nAs informacoes atualizadas nao foram salvas em arquivo. \n \nPor isso elas serao listadas abaixo:");
            System.out.println(" \n========== Novas Coordenadas dos Centroides ========== ");
            imprimirPontos(dadosCentroit, false);
            return;
        }

        try (FileWriter arq = new FileWriter(caminhoArquivo.trim())) {
            PrintWriter gravarArq = new PrintWriter(arq);

            for (int i = 0; i < dadosCentroit.getPontos().size(); i++) {
                coordenadasAux = dadosCentroit.getPontos().get(i).getCoordenada().toString();
                coordenadasAux = coordenadasAux.replace("[", "");
                coordenadasAux = coordenadasAux.replace("]", "");
                coordenadasAux = coordenadasAux.replace(" ", "");
                textoArquivo.append(coordenadasAux.trim()).append("\n");
            }

            gravarArq.printf(textoArquivo.toString());
        }
        System.out.println(" \nDados processados e arquivo salvo com sucesso!");
    }
}
