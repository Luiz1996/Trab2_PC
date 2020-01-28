/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uem.din.pc.main;

import br.uem.din.pc.controller.ArquivoController;
import br.uem.din.pc.controller.PontosControllerMPI;
import br.uem.din.pc.model.PontosModel;
import java.io.FileNotFoundException;
import java.io.IOException;
import mpi.MPI;

/**
 *
 * @author Luiz Flávio
 */
public class Main {

    public static int PROCESSO_MESTRE = 0;
    public static long tempoInicial = 0;
    public static long tempoFinal = 0;
    public static PontosModel dadosArqBase = new PontosModel();
    public static PontosModel dadosArqCentroide = new PontosModel();
    public static int houveAlteracao = 1;

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, Exception {
        //inicializando MPI
        MPI.Init(args);

        //validando quantidade de processos a executar a aplicação
        if(MPI.COMM_WORLD.Size() < 2){
            System.out.println("Devem haver pelo menos 3 processos para executar esta aplicacao em paralelo.\nPrograma Finalizado!");
            System.exit(0);
        }
        
        //este método realizará a leitura do arquivo de base e centróide  
        ArquivoController.lerArquivos(Integer.parseInt(args[3]));

        //barreira inicial para início da contagem do tempo de execução
        MPI.COMM_WORLD.Barrier();

        //coletando tempo inicial
        if (MPI.COMM_WORLD.Rank() == 0) {
            System.out.println(" \n ========== K-Means - Paralelizado com MPI/MPJ Express ========== ");
            System.out.println(" \nBiblioteca disponibilizada em: http://mpj-express.org/");
            System.out.println(" \nidProcessoMestre: " + PROCESSO_MESTRE + " | qtdeProcessosServos: " + (MPI.COMM_WORLD.Size() - 1) + " | qtdeTotalProcessos: " + MPI.COMM_WORLD.Size() + ".");
            System.out.println(" \nIniciando processamento do K-Means...");
            tempoInicial = System.currentTimeMillis();
        }

        //realizando o processamento do algorítmo de K-Means com MPI
        PontosControllerMPI.calcularKMeansMPI();

        //coletando tempo final, finalizando aplicação
        if (MPI.COMM_WORLD.Rank() == 0 && houveAlteracao <= 0) {
            tempoFinal = System.currentTimeMillis();
            
            System.out.println(" \nProcessamento do K-Means foi finalizado com sucesso! \n \nA execucao do programa levou " + (tempoFinal - tempoInicial) + " milissegundos.");
            
            ArquivoController.escreverArquivoCentroideAtualizado(dadosArqCentroide);
        }

        //finalizando MPI
        MPI.Finalize();
        
        //chamando do garbage collector
        System.gc();
    }
}
