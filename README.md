# Biblioteca Virtual

Projeto de estudo em Java e Spring Boot para organizar uma biblioteca pessoal e acompanhar leituras.

## Tecnologias

- Java 21 e Spring Boot 4.1.1
- Maven Wrapper
- Spring Web MVC, Data JPA, Security e Validation
- PostgreSQL no Supabase e Flyway para migrações
- HTML, CSS e JavaScript entregues pelo próprio Spring Boot

## Preparar o ambiente

Abra a pasta que contém o `pom.xml` no VS Code e confira:

```sh
java -version
./mvnw -version
```

O Wrapper utiliza a versão definida em `.mvn/wrapper/maven-wrapper.properties`; não é necessário instalar o comando `mvn` separadamente. A primeira execução pode baixar dependências para o cache `~/.m2`.

## Conectar ao Supabase

1. No painel do projeto, abra **Connect → Session pooler**, porta `5432`. Consulte o host, banco e usuário; a senha é a do banco, não a senha da conta nem uma chave de API.
2. Na pasta do `pom.xml`, copie o modelo:

   ```sh
   cp -n application-local.properties.example application-local.properties
   ```

3. Preencha e salve somente `application-local.properties`, removendo os colchetes. Usuário e senha ficam nas propriedades próprias, fora da URL. Mantenha `sslmode=require`. Em `.properties`, uma barra invertida literal na senha deve ser escrita duas vezes.
4. Confira que o arquivo privado está ignorado:

   ```sh
   git check-ignore application-local.properties
   ```

Um perfil agrupa configurações de um ambiente. O perfil `local` carrega esse arquivo externo quando os comandos são executados na pasta do `pom.xml`. Configure a cópia privada separadamente em cada Mac. O arquivo `.example` contém apenas valores fictícios.

Não versione senhas nem use `git add -f` no arquivo privado. O `.gitignore` também exclui `.env` e `.env.*`, exceto `.env.example`. O Spring Boot não carrega `.env` automaticamente.

Referência: [Supabase com Spring Boot](https://supabase.com/docs/guides/getting-started/quickstarts/spring-boot).

## Executar

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Abra [http://localhost:8080](http://localhost:8080), ou a porta que você configurou. Encerre com `Ctrl+C`.

O cadastro envia nome, e-mail e senha ao backend e salva a conta no Supabase. A senha é armazenada somente como hash PBKDF2 com salt aleatório, calculado pelo `PasswordEncoder` do Spring Security. O sucesso do cadastro não inicia uma sessão autenticada. A aba de login continua sendo uma prévia e não envia seus dados.

## Migrações do banco

Ao iniciar a aplicação ou os testes de integração, o Flyway cria o schema `app` e executa as migrações pendentes de `src/main/resources/db/migration`. A primeira cria `app.usuarios`, com identificador UUID, nome, e-mail único e hash da senha. O histórico fica em `app.flyway_schema_history`.

O Hibernate permanece com `ddl-auto=none`; a estrutura é controlada pelo SQL versionado. Não edite uma migração já aplicada: crie uma nova versão para as próximas alterações. A tabela usa RLS, sem políticas públicas; o acesso do backend utiliza a conexão PostgreSQL configurada. Não é uma integração com o Supabase Auth.

## Fluxo do cadastro

- `Usuario`: entidade JPA mapeada para `app.usuarios`. O construtor público recebe o hash já calculado; o construtor protegido permite ao JPA carregar registros. Os getters permitem consultar os dados.
- `CadastroUsuarioRequest`: DTO de entrada, com validação de nome, e-mail e senha. Remove espaços nas pontas do nome/e-mail e normaliza o e-mail para minúsculas; preserva a senha. A senha deve ter de 8 a 128 caracteres.
- `UsuarioController`: recebe `POST /api/usuarios` e retorna HTTP 201 ao criar a conta.
- `UsuarioService`: verifica e-mail existente, calcula o hash e salva dentro de uma transação.
- `UsuarioRepository`: fornece operações de persistência e busca por e-mail usando Spring Data JPA. A restrição única do banco também impede duplicação em requisições simultâneas.
- `UsuarioResponse`: devolve somente id, nome e e-mail, sem senha ou hash.
- `ApiExceptionHandler`: retorna HTTP 400 para entrada inválida, 409 para e-mail existente e uma mensagem genérica para falhas do banco. Não expõe valores rejeitados, SQL ou credenciais.
- `CsrfController`: entrega em `GET /api/csrf` um token da sessão, sem cache. O JavaScript obtém esse token e envia seu valor no cabeçalho do cadastro.
- `SecurityConfig`: libera a interface, o token CSRF e o POST de cadastro. As outras rotas exigem autenticação. CSRF continua ativo; não há login real nesta etapa.

O frontend fica em `src/main/resources/static`: HTML define o conteúdo, CSS cuida do visual e JavaScript controla os formulários e as requisições. Ele bloqueia envios repetidos durante o cadastro, mostra erros sem interpretar HTML recebido e limpa os campos após o sucesso. Os dados não são guardados em localStorage.

## Testar

```sh
./mvnw test -Dspring.profiles.active=local
```

Os testes da interface verificam arquivos públicos e proteção das rotas. Os testes do controller cobrem validação, status HTTP, CSRF, e-mail duplicado e respostas sem dados sensíveis. O teste de integração usa o PostgreSQL configurado, obtém um token CSRF real, cadastra uma conta fictícia, verifica a persistência e o hash e testa a duplicação. A transação de dados desse teste é desfeita ao terminar; as migrações permanecem aplicadas.

Relatórios e arquivos de compilação ficam em `target/`, ignorado pelo Git. Testes MockMvc não substituem a verificação do JavaScript no navegador.

Verificação desta etapa: 12 testes Maven aprovados, incluindo gravação no PostgreSQL e verificação do hash em transação com rollback; migração V1 aplicada. O JavaScript passou por `node --check`. No Chrome, em desktop e celular (1440/390/320 pixels), foram verificados o token CSRF real, o corpo JSON, o bloqueio de envios repetidos, a limpeza após sucesso, as mensagens para HTTP 400/409/403/500 e falhas de rede e a recuperação do botão. Os POSTs dessa verificação de navegador foram interceptados com respostas simuladas; ela não criou contas permanentes. O teste de integração Maven foi o responsável pela verificação real de persistência.

## Git e desenvolvimento

Trabalhamos em etapas pequenas: explicar, implementar, testar e revisar o diff antes do commit. Commits e pushes exigem autorização. Funcionalidades maiores usam branches próprias; o cadastro é desenvolvido em `feat/cadastro-usuarios`, separado da `main`.

O commit registra uma versão local. O push envia commits ao GitHub. O merge integra as alterações de uma branch a outra; esta etapa ainda não faz merge automático.
