CREATE TABLE tipos_insumo (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE unidades (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE,
    nome VARCHAR(60) NOT NULL,
    dimensao VARCHAR(10) NOT NULL,
    fator_base DECIMAL(18,6) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_unidade_dimensao CHECK (dimensao IN ('MASSA','VOLUME','UNIDADE')),
    CONSTRAINT ck_unidade_fator CHECK (fator_base > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE insumos (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    tipo_id BIGINT NOT NULL,
    unidade_compra_id BIGINT NOT NULL,
    quantidade_compra DECIMAL(18,6) NOT NULL,
    preco_compra DECIMAL(18,2) NULL,
    preco_atualizado_em DATETIME(6) NULL,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    arquivado_em DATETIME(6) NULL,
    versao BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_insumo_tipo FOREIGN KEY (tipo_id) REFERENCES tipos_insumo(id) ON DELETE RESTRICT,
    CONSTRAINT fk_insumo_unidade FOREIGN KEY (unidade_compra_id) REFERENCES unidades(id) ON DELETE RESTRICT,
    CONSTRAINT ck_insumo_quantidade CHECK (quantidade_compra > 0),
    CONSTRAINT ck_insumo_preco CHECK (preco_compra IS NULL OR preco_compra >= 0),
    CONSTRAINT ck_insumo_preco_data CHECK ((preco_compra IS NULL AND preco_atualizado_em IS NULL) OR (preco_compra IS NOT NULL AND preco_atualizado_em IS NOT NULL)),
    CONSTRAINT ck_insumo_versao CHECK (versao >= 1),
    INDEX ix_insumo_nome_id (nome, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO tipos_insumo (codigo,nome) VALUES ('INGREDIENTE','Ingrediente'),('EMBALAGEM','Embalagem');
INSERT INTO unidades (codigo,nome,dimensao,fator_base) VALUES
('g','Grama','MASSA',1),('kg','Quilograma','MASSA',1000),
('ml','Mililitro','VOLUME',1),('l','Litro','VOLUME',1000),('un','Unidade','UNIDADE',1);
