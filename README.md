# LMC – Livro de Movimentação de Combustíveis

## Visão geral
Aplicação **Spring Boot 3** responsável por centralizar o registro diário das movimentações de combustíveis (LMC) de um posto. O backend expõe APIs REST para cadastro das folhas diárias, consulta de relatórios e exportação em PDF. Um bundle estático (gerado em Vite) é servido pelo próprio Spring para a interface web.

## Principais funcionalidades
- Cadastro de folhas diárias do LMC com medições de tanques, compras e vendas por bico.
- Validação automática de variações de estoque e obrigatoriedade de observações quando o limite regulamentar de 0,6% é excedido.
- Consulta de relatórios consolidados por período, com carregamento ávido das associações relevantes.
- Geração de relatório em **PDF** utilizando JasperReports (template `reports/lmc_anexo.jrxml`).
- Endpoints auxiliares para listar produtos, tanques e bicos configurados.

## Stack tecnológica
- Java 21 + Spring Boot 3 (Web, Data JPA, Validation)
- PostgreSQL (persistência relacional)
- JasperReports (relatórios em PDF)
- Maven Wrapper (`mvnw`) para build/testes
- Docker Compose para ambiente completo (app + banco)

## Pré-requisitos
Para execução local sem Docker:
- Java 21
- Maven (ou utilizar o wrapper `./mvnw`)
- PostgreSQL 16 ou compatível, com um banco chamado `lmc_db` e credenciais `postgres/postgres` (ajuste em `src/main/resources/application.properties` se necessário)

## Executando a aplicação
### Usando Docker Compose
```bash
docker compose up --build
```
O serviço backend estará disponível em `http://localhost:8080` e o PostgreSQL em `localhost:5432`.

### Usando Maven
1. Garanta que o PostgreSQL esteja em execução e com as credenciais configuradas.
2. Rode o backend:
   ```bash
   ./mvnw spring-boot:run
   ```
3. A aplicação será exposta em `http://localhost:8080`.

## Endpoints principais
| Método | Rota | Descrição |
| ------ | ---- | --------- |
| `POST` | `/api/lmc` | Cadastra uma folha diária do LMC. |
| `GET` | `/api/lmc/relatorio?inicio=YYYY-MM-DD&fim=YYYY-MM-DD` | Retorna lista de folhas dentro do intervalo informado. |
| `GET` | `/api/lmc/relatorio/pdf?inicio=YYYY-MM-DD&fim=YYYY-MM-DD` | Faz download do relatório consolidado em PDF. |
| `GET` | `/api/config/produtos` | Lista produtos cadastrados. |
| `GET` | `/api/config/tanques?produtoId={id}` | Lista tanques associados ao produto. |
| `GET` | `/api/config/bicos?tanqueId={id}` | Lista bicos associados ao tanque. |

### Exemplo de payload para `POST /api/lmc`
```json
{
  "data": "2024-03-14",
  "produtoId": 1,
  "observacoes": "Ajuste devido à aferição extraordinária",
  "medicoes": [
    {
      "tanqueId": 10,
      "estoqueAbertura": 1234.50,
      "estoqueFechamentoFisico": 1180.20
    }
  ],
  "compras": [
    {
      "tanqueDescargaId": 10,
      "numeroDocumentoFiscal": "NF-12345",
      "volumeRecebido": 500.00
    }
  ],
  "vendas": [
    {
      "bicoId": 25,
      "precoNaBomba": 5.89,
      "encerranteAbertura": 102030,
      "encerranteFechamento": 102560,
      "afericoes": 5.50
    }
  ]
}
```

## Estrutura do projeto
```
src/main/java/com/example/lmc
├── config              # Configurações auxiliares
├── controller          # Endpoints REST e roteamento SPA
├── dto                 # Objetos de transferência usados pelas APIs
├── entity              # Entidades JPA (Produto, Tanque, Bico, LmcFolha, etc.)
├── exception           # Tratadores globais de exceções
├── repository          # Interfaces Spring Data JPA
└── service             # Regras de negócio e geração de relatórios
```
Arquivos estáticos do front-end ficam em `src/main/resources/static` e o template JasperReports em `src/main/resources/reports`.

## Relatórios em PDF
O template padrão (`reports/lmc_anexo.jrxml`) define cabeçalho e colunas do anexo oficial do LMC. Para personalização, edite o arquivo no JasperSoft Studio ou outro editor compatível e recompile com a aplicação.

## Testes
Execute os testes automatizados (quando disponíveis) com:
```bash
./mvnw test
```

## Contribuição
1. Crie um branch a partir da `main`.
2. Implemente e teste suas alterações.
3. Abra um Pull Request descrevendo o contexto e as validações executadas.

---
> Dúvidas ou sugestões? Abra uma issue no repositório.
