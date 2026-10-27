# Biblioteca Virtual

Projeto de estudo em Java e Spring Boot para organizar uma biblioteca pessoal e acompanhar leituras.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Maven pelo Maven Wrapper
- Spring Web MVC, Spring Data JPA, Spring Security e Validation
- Driver PostgreSQL; conexão com o banco ainda não configurada

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

A dependência do driver PostgreSQL já está declarada. Falta configurar a conexão com o banco: atualmente, `application.properties` contém apenas o nome da aplicação e não há banco em memória configurado. O próximo passo é configurar PostgreSQL/Supabase e executar o teste novamente.

Os arquivos gerados pela compilação e os relatórios dos testes ficam em `target/`, ignorado pelo Git.

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
