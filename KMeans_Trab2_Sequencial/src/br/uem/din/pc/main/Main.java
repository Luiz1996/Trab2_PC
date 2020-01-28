/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uem.din.pc.main;

import br.uem.din.pc.controller.ArquivoController;
import br.uem.din.pc.controller.PontosController;
import br.uem.din.pc.model.PontosModel;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Luiz Flávio
 */
public class Main {

    public static int PROCESSO_MESTRE = 0;
    public static long tempoInicial = 0;
    public static PontosModel dadosArqBase = new PontosModel();
    public static PontosModel dadosArqCentroide = new PontosModel();
    public static boolean houveAlteracao = true;

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, Exception {
        //este método realizará a leitura do arquivo de base e centróide  
        ArquivoController.lerArquivos(Integer.parseInt(args[0]));
        
        tempoInicial = System.currentTimeMillis();
        
        //realizando o processamento do algorítmo de K-Means Sequencialmente
        PontosController.calculaKMeansSequencial(dadosArqBase, dadosArqCentroide);

        System.out.println("Processamento do K-Means foi finalizado com sucesso!\n\nA execucao do programa levou " + (System.currentTimeMillis() - tempoInicial) + " milissegundos.");
            
        //salvando as novas informações em arquivo
        ArquivoController.escreverArquivoCentroideAtualizado(dadosArqCentroide);
        
        //chamando do garbage collector
        System.gc();
    }
}
