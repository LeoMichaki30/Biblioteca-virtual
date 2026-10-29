CREATE TABLE app.usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_nome CHECK (char_length(btrim(nome)) > 0),
    CONSTRAINT ck_usuarios_email_normalizado CHECK (email = lower(btrim(email)))
);

-- O acesso aos usuários ocorre pelo backend, com a conexão do PostgreSQL.
ALTER TABLE app.usuarios ENABLE ROW LEVEL SECURITY;
