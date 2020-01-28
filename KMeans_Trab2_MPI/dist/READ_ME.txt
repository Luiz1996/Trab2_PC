# Instalação Windows

Para conseguir rodar nosso projeto, você vai precisar instalar o mpj em sua máquina. 
Para isso primeiramente faça o download da biblioteca MPJ em sua máquina atráves do link: [http://mpj-express.org/download.php](http://mpj-express.org/download.php)
Estamos utilizando a versão 0.44 para a execução do nosso programa.
Após o download do mpj, recomendamos que faça a extração do zip na raiz do diretório C:/ e renomeie a pasta descompactada para somente "mpj". Isso irá facilitar a configuração e da execução do programa.
Além desse download, para o programa ler os arquivos de bases e centroit, deve ser deixado todos eles dentro de uma pasta chamada "bases_programacao_concorrente", também no diretório c:/. Os nomes dos arquivos estão padronizados conforme o professor nos repassou.

Precisaremos agora configurar duas váriaveis de ambiente. Então teremos que abrir uma janela do windows explorer(win+e). Na árvore de pastas encontre a opção "Este Computador" e clique com o botão direito, selecione a opção "Propriedades". Caso não consiga encontrar as opções acima, você pode procurar em seu teclado a opção "Pause Break" e apertar "Win+Pause Break" que irá para o mesmo lugar. Clique em "Configurações avançadas do sistema" em seguida clique em "Variáveis de ambiente...". Com a janela de variáveis aberta, vamos incluir uma nova variável na sessão "Variáveis de usuário para User", clique em "Novo...", insira no Nome da váriavel o valor "MPJ_HOME" e na frente o caminho até aonde vc descompactou a biblioteca.
Clique em "OK"! Feito isso vamos criar outra váriavel agora o nome será "CLASSPATH", essa terá o caminho de onde você descompactou a biblioteca mais o final "\lib". E para finalizar iremos criar a última variável, esse terá o nome "PATH", terá o mesmo caminho aonde foi descompactado a biblioteca, mais o final "\bin". Feito isso abra a pasta da biblioteca vá dentro da pasta "bin" e execute os dois arquivos com a extesão .bat que se encontra nestra pasta. Para finalizar reinicie sua máquina. Pronto, agora sua máquina está habilitada para rodar a biblioteca MPJ.

Para nosso programa funcionar, foi feito um arquivo .bat para facilitar a compilação e execução. Este arquivo se encontra na raiz do nosso projeto com o nome "build.bat", para funcionar deve ser criado duas pastas na raiz do diretório a pasta "build" e a pasta "dist". Estas pastas estarão envolvidas no processo de compilação e geração de um .jar do nosso programa.
Rodando o nosso bat, ele irá juntar todos os nossos arquivos fazer a compilação pelo comando javac, neste comando ele faz a inclusão da biblioteca do mpj via comando -cp mais o diretório aonde fica o mpj.jar, essa compilação ocorre dentro da pasta build.
Após compilar o programa nossso arquivo .bat faz a criação do arquivo .jar utilizando o comando jar cmf, e armazena o .jar dentro da pasta dist.
Com isso ele já faz a execução do programa com 4 processos(esse valor é configurável) e utilizando o arquivo base 59.

Caso seja necessário executar o programa alguma outra vez, você pode executar o bat novamente, para evitar o processo manual, ou pode abrir o cmd ir até a pasta aonde o .jar está e executar o seguinte comando:
java -jar path_to_mpj\lib\starter.jar -np X KMeans.jar Y

Para explicar o comando, o inicio dele é padrão e não tem como alterar, então java -jar é para o java instalado em sua máquina executar um arquivo jar.
"path_to_mpj\lib\starter.jar" é o caminho para a pasta da biblioteca mpj que instalamos em sua máquina no início da explicação e dentro desta pasta existe uma pasta lib e dentro da lib tem o arquivo starter.jar que é o arquivo que iremos utilizar para execução do mpj. O parâmetro -np indica a quantidade de processos que utilizaremos na execução do nosso programa, caso queira mudar a quantidade basta mexer nesta opção que é representado pela letra X, em seguida temos o caminho para o arquivo .jar que iremos executar, neste caso como está na mesma basta, masta indicar o nome. E para finalizar temos a opção Y(que é o args[]) que diz respeito a qual arquivo iremos execução.

1 -> 59 coordenadas
2 -> 161 coordenadas
3 -> 256 coordenadas
4 -> 1380 coordenadas
5 -> 1601 coordenadas