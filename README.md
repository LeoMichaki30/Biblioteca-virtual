# Biblioteca Virtual

Projeto de estudo em Java e Spring Boot para organizar uma biblioteca pessoal e acompanhar leituras.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Maven pelo Maven Wrapper
- Spring Web MVC, Spring Data JPA, Spring Security e Validation
- PostgreSQL no Supabase; conexão local validada

## Preparar o ambiente

Abra a pasta `biblioteca` no VS Code. Ela contém o `pom.xml` e o repositório Git do projeto.

Confira o Java e o Maven Wrapper no terminal dessa pasta:

```sh
java -version
./mvnw -version
```

O Wrapper utiliza a versão do Maven definida em `.mvn/wrapper/maven-wrapper.properties`. Não é necessário instalar o comando `mvn` separadamente. A primeira execução pode precisar de internet para baixar o Maven e as dependências; o cache fica normalmente em `~/.m2`.

## Executar os testes

```sh
./mvnw test
```

O teste `BibliotecaApplicationTests.contextLoads()` verifica se o contexto do Spring consegue iniciar, incluindo a configuração de acesso ao banco.

Na validação inicial, a aplicação e o teste compilaram com Java 21, mas o teste apresentou erro na inicialização: `Failed to determine a suitable driver class`.

A dependência do driver PostgreSQL já está declarada. A conexão é configurada pelo perfil `local`, conforme as instruções abaixo. Em 29/10/2026, após preencher e salvar o arquivo local, a conexão com o Supabase e a inicialização do JPA foram validadas: `./mvnw test -Dspring.profiles.active=local` terminou com `BUILD SUCCESS` (1 teste, nenhuma falha ou erro). Esse teste verifica a inicialização; ele não comprova operações de cadastro e consulta no banco.

Os arquivos gerados pela compilação e os relatórios dos testes ficam em `target/`, ignorado pelo Git.

## Configurar PostgreSQL/Supabase no Mac

Um perfil do Spring reúne configurações para um ambiente. O perfil `local` usa as configurações particulares deste computador.

1. No projeto Supabase, abra **Connect** e escolha **Session pooler** (porta `5432`). Anote host, nome do banco e usuário exatamente como aparecem no painel. Use a senha do banco, não a senha da conta Supabase nem uma chave de API.
2. Copie o modelo na pasta que contém o `pom.xml`:

   ```sh
   cp -n application-local.properties.example application-local.properties
   ```

3. Abra `application-local.properties` no VS Code e substitua os valores entre colchetes. A URL não deve conter usuário nem senha, pois eles têm propriedades próprias. Mantenha `sslmode=require` para exigir conexão criptografada. Em arquivos `.properties`, uma barra invertida literal na senha precisa ser escrita duas vezes.
4. Confira que o Git ignora o arquivo privado:

   ```sh
   git check-ignore application-local.properties
   ```

   O comando deve imprimir `application-local.properties`. Não use `git add -f` nesse arquivo.
5. Execute o teste com o perfil ativo:

   ```sh
   ./mvnw test -Dspring.profiles.active=local
   ```

6. Para iniciar a aplicação com a mesma configuração:

   ```sh
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

O Spring Boot procura configurações externas na pasta de execução; por isso, execute os comandos na pasta do `pom.xml`. Sem ativar o perfil `local`, esse arquivo não é carregado.

O modelo limita o pool a duas conexões e usa `ddl-auto=none`: esta etapa apenas configura o acesso, sem criar ou alterar tabelas automaticamente. A estrutura das tabelas será tratada quando implementarmos as entidades.

O modelo `.example` contém apenas valores fictícios e pode ser versionado. O arquivo preenchido é privado e deverá ser configurado separadamente em cada Mac. Não envie sua senha pelo chat.

Referência: [Supabase com Spring Boot](https://supabase.com/docs/guides/getting-started/quickstarts/spring-boot).

## Iniciar a aplicação

Depois de configurar a conexão com o banco:

```sh
./mvnw spring-boot:run
```

A classe `BibliotecaApplication` contém o método `main` que inicia o Spring Boot. O arquivo `Main.java` na pasta acima de `biblioteca` é um exercício separado e não faz parte desta aplicação Maven.

## Credenciais

Não coloque senhas ou tokens no código, neste README ou nos commits. A configuração do banco deverá usar valores externos ao repositório.

O `.gitignore` já exclui `.env`, `.env.*` (exceto `.env.example`) e `application-local.properties`. Um arquivo `.env` não é carregado automaticamente pelo Spring Boot; será necessário configurar explicitamente como os valores chegam à aplicação.

## Fluxo de desenvolvimento

1. Explicar e implementar uma etapa pequena.
2. Executar as verificações e explicar eventuais erros.
3. Revisar os arquivos alterados com `git status` e `git diff`.
4. Sugerir uma mensagem de commit no padrão Conventional Commits.
5. Criar o commit e fazer push somente após autorização do responsável pelo projeto.

O commit registra uma versão no repositório local. O push envia os commits locais ao GitHub.

## Modelo de usuários

A classe `usuario.Usuario` representa os dados de uma conta: `id` (UUID gerado pelo JPA), `nome`, `email` e `senhaHash`. O construtor público recebe um hash já calculado; ele não transforma nem valida uma senha original. Essa responsabilidade será implementada no serviço de cadastro com um `PasswordEncoder`.

`@Entity` identifica a classe como entidade JPA. `@Table` define o mapeamento para `app.usuarios`, separado do schema público do Supabase. As anotações `@Column` descrevem as restrições esperadas para as colunas, incluindo e-mail único; elas não substituem a validação dos dados recebidos pela API. `@JsonIgnore` impede que o hash seja incluído na serialização JSON padrão.

O construtor protegido sem argumentos é usado pelo JPA ao carregar registros. Os métodos `get...` permitem consultar os campos sem oferecer alterações diretas por setters.

Esta etapa define apenas o modelo Java. O schema `app` e a tabela `usuarios` ainda precisam ser criados por uma migração; a configuração local mantém `ddl-auto=none`. Ainda não há repositório, serviço, rota de cadastro ou login. O teste de contexto verifica se o Spring aceita o mapeamento, sem testar a persistência da entidade.

## Primeira interface: cadastro e login

Inicie pelo terminal na pasta do `pom.xml`:

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Abra [http://localhost:8080](http://localhost:8080). Para encerrar a aplicação, use `Ctrl+C` no terminal. Se você configurou outra porta localmente, use essa porta no endereço.

Esta é uma prévia visual: os formulários validam campos no navegador, mas não criam contas, não autenticam e não enviam nem salvam os valores. Use dados fictícios para experimentar. Nenhuma nova tabela é criada nesta etapa.

- `static/index.html`: estrutura, textos e formulários de cadastro e entrada.
- `static/css/biblioteca.css`: cores, layout, ilustração dos livros e adaptação para celular.
- `static/js/biblioteca.js`: alternância de formulários, confirmação de senha e botão de mostrar senha. O envio é interceptado e exibe uma mensagem de prévia; os campos são limpos após a validação.
- `config.SecurityConfig`: permite acesso público por GET somente à página inicial e aos arquivos da interface. As outras rotas exigem autenticação e a proteção CSRF continua ativa. A autenticação real será implementada em outra etapa.
- `PaginaInicialTests`: verifica acesso público à interface, acesso aos arquivos estáticos, proteção de outras rotas e bloqueio de POST sem token CSRF. Esses testes verificam as respostas do Spring, não executam o JavaScript em um navegador.

HTML define o conteúdo; CSS define a apresentação; JavaScript cuida das interações. O Spring Boot entrega esses arquivos a partir de `src/main/resources/static`, sem adicionar um framework de frontend ou outro processo de build.

### Verificações desta interface

A suíte Maven com o perfil `local` passou: 5 testes, nenhuma falha ou erro. O JavaScript passou pela verificação de sintaxe com `node --check`.

Também foi feita uma verificação no Chrome, com dados fictícios, em larguras de 1440, 390 e 320 pixels: sem rolagem horizontal; alternância por teclado, confirmação de senha, validação de e-mail, mostrar/ocultar senha e limpeza dos campos funcionando. Os envios de prévia não geraram requisições de rede e uma rota privada retornou HTTP 401. Essa verificação de navegador foi executada separadamente e não faz parte da suíte Maven.
