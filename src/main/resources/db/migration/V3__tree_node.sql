CREATE TABLE tree_node (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        INTEGER      NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    descrizione VARCHAR(255) NOT NULL,
    parent_code INTEGER      NULL,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT pk_tree_node       PRIMARY KEY (id),
    CONSTRAINT uq_tree_node_code  UNIQUE (code),
    CONSTRAINT fk_tree_node_parent FOREIGN KEY (parent_code) REFERENCES tree_node (code) ON DELETE CASCADE
);

INSERT INTO tree_node (code, type, descrizione, parent_code, sort_order) VALUES
(10,  'Complesso',   'Sicurezza Nazionale',     NULL, 0),
(20,  'Complesso',   'Innovazione Tecnologica', NULL, 1),
(11,  'Area',        'Intelligence',            10,   0),
(12,  'Area',        'Difesa',                  10,   1),
(21,  'Area',        'Cybersecurity',           20,   0),
(22,  'Area',        'Intelligenza Artificiale',20,   1),
(111, 'Trattazione', 'Analisi Strategica',      11,   0),
(112, 'Trattazione', 'Controspionaggio',        11,   1),
(121, 'Trattazione', 'Operazioni Militari',     12,   0),
(211, 'Trattazione', 'Minacce Informatiche',    21,   0),
(212, 'Trattazione', 'Sicurezza delle Reti',    21,   1),
(221, 'Trattazione', 'Machine Learning',        22,   0);
