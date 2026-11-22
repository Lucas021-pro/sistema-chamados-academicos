# Sistema de Gestão de Chamados Acadêmicos

Aplicação web full-stack e API REST para abertura, triagem, acompanhamento, análise de indicadores e exportação de chamados acadêmicos. Projeto desenvolvido como trabalho final da disciplina de **Linguagem de Programação 2 (LGP2)** do curso de Bacharelado em Sistemas de Informação do **Instituto Federal de São Paulo (IFSP) — Campus São Paulo**.

## Autor

* **Lucas Eduardo de Araujo Silva** — Prontuário: `SP3208303`

---

## Tecnologias Utilizadas

* **Linguagem:** Java 21 (`records`, *switch expressions*, *pattern matching*)
* **Framework Backend:** Spring Boot 3.3.5 (Spring Web MVC, Spring Data JPA, Jakarta Bean Validation)
* **Banco de Dados:** H2 Database (persistência em arquivo `./dados/chamados` e banco em memória para testes)
* **Frontend:** Thymeleaf, HTML5, Tailwind CSS e JavaScript (gráficos SVG interativos, filtros dinâmicos e modo escuro)
* **Exportação de Relatórios:** Apache PDFBox (`.pdf`), Apache POI (`.xlsx`) e gerador CSV nativo em UTF-8 com BOM (`.csv`)
* **Testes Automatizados:** JUnit 5, Mockito e Spring Boot Test (`@DataJpaTest`)

---

## Arquitetura do Projeto

O sistema segue a arquitetura em camadas, compartilhando as regras de negócio (`Service`), validações (`DTO`) e criação de entidades (`Factory`) tanto para a interface web (MVC) quanto para a API REST:

```text
src/
├── main/
│   ├── java/br/edu/ifsp/lgp2/
│   │   ├── config/          # Carga inicial (DadosIniciaisConfig), SessaoInterceptor e WebConfig
│   │   ├── controller/      # Controllers MVC (ChamadoController, RelatorioController, LoginController)
│   │   │                    # e API REST (ChamadoApiController)
│   │   ├── dto/             # Objetos de transferência, validação Bean Validation e records de resposta
│   │   ├── exception/       # Tratamento global de erros da API (ApiExceptionHandler e ErroApiResponse)
│   │   ├── factory/         # ChamadoFactory (encapsula criação, prioridade, responsável e histórico inicial)
│   │   ├── model/           # Entidade JPA Chamado, @Embeddable HistoricoAlteracao e ValoresChamado
│   │   ├── repository/      # ChamadoRepository (JpaRepository + JpaSpecificationExecutor)
│   │   ├── service/         # ChamadoService, ConsultaService (Specifications) e ExportacaoService
│   │   └── ProjetoDtoStatusChamadosApplication.java
│   └── resources/
│       ├── static/js/       # Alternância de tema claro/escuro (tema.js)
│       ├── templates/       # Páginas Thymeleaf (login, chamados/*, indicadores, relatorios, fragments)
│       └── application.properties
└── test/
    └── java/br/edu/ifsp/lgp2/
        ├── factory/         # ChamadoFactoryTest
        ├── service/         # ChamadoServiceTest, ConsultaServiceTest, ExportacaoServiceTest
        └── ProjetoApplicationTests.java
```

## Funcionalidades Principais

* **Controle de Acesso Demonstrativo:** Autenticação baseada em sessão HTTP (`LoginController` + `SessaoInterceptor`), redirecionando acessos não autenticados na web para `/login` e retornando `401 Unauthorized` nas rotas `/api/**`.
* **Ciclo de Vida Completo de Chamados (CRUD):**
    * **Abertura (`/chamados/novo`):** Classificação por assunto (*Matrícula*, *Documentos*, *Suporte técnico* e *Atendimento acadêmico*), atribuindo automaticamente prioridade inicial e equipe responsável via `ChamadoFactory`.
    * **Edição (`/chamados/editar/{id}`):** Atualização dos dados da solicitação preservando status e data de abertura.
    * **Fluxo de Atendimento (`/chamados/status/{id}`):** Transição entre `ABERTO`, `EM_ANALISE`, `CONCLUIDO` e `CANCELADO` com observação obrigatória.
    * **Exclusão (`/chamados/excluir/{id}`):** Remoção com modal de confirmação.
* **Linha do Tempo de Auditoria:** Cada abertura, edição de dados ou alteração de status registra data/hora, responsável, status e observação no histórico do chamado (`chamado_historico`).
* **Busca e Filtros Dinâmicos:** Filtragem combinada via `Specification<Chamado>` por texto livre (título ou descrição), busca direta por `#ID` (ex.: `#4`), status, categoria, prioridade e intervalo de datas.
* **Indicadores e Relatórios Gráficos:**
    * Painel de indicadores (`/indicadores`) com recorte temporal (*7 dias*, *30 dias*, *Semestre* e *Todos*).
    * Tela de relatórios (`/relatorios`) com cards de KPI, gráfico de evolução temporal (linhas), distribuição por prioridade (rosca) e volume por categoria (barras), construídos em SVG.
* **Exportação de Dados Filtrados:** Gera arquivos **PDF** (tabela paginada em A4 paisagem), **Excel (`.xlsx`)** e **CSV** (`;`, UTF-8 com BOM) respeitando exatamente os filtros ativos na tela.

---

## Como Executar o Projeto

### Pré-requisitos
* **JDK 21** ou superior instalado
* **Maven 3.9+** (ou uso do wrapper `./mvnw` / `mvnw.cmd`)

### Passos para execução

1. Clone o repositório e acesse a pasta do projeto:
   ```bash
   git clone <url-do-repositorio>
   cd Sistema_Gestao_Chamados_Academicos_Java21
   ```

2. Execute a aplicação com o Maven:
   ```bash
   mvn spring-boot:run
   ```

3. Acesse no navegador:
   ```text
   http://localhost:8081
   ```

4. Utilize as credenciais de demonstração na tela de login:
    * **Usuário:** `admin`
    * **Senha:** `admin123`

> **Nota sobre dados iniciais:** Ao iniciar com o banco vazio, a classe `DadosIniciaisConfig` popula automaticamente 12 chamados de exemplo distribuídos ao longo dos últimos 60 dias com histórico completo. Caso queira restaurar a base inicial limpa, encerre a aplicação e remova a pasta `./dados` na raiz do projeto.

---

## Como Executar os Testes Automatizados

Para rodar toda a suíte de testes unitários e de integração JPA:

```bash
mvn test
```

Os testes cobrem:
* **`ChamadoFactoryTest`:** Regras de prioridade, equipe responsável, datas e registro do evento inicial por categoria.
* **`ChamadoServiceTest`:** Persistência de novos chamados, edição preservando estado/abertura e registro de histórico nas atualizações.
* **`ConsultaServiceTest`:** Consultas dinâmicas com `Specification`, busca exata por `#ID` e cálculo de indicadores por período.
* **`ExportacaoServiceTest`:** Geração válida de CSV (com proteção contra *formula injection*), planilhas `.xlsx` e documentos `.pdf`.

---

## Documentação da API REST (`/api/chamados`)

Todos os endpoints da API compartilham as mesmas regras de negócio da aplicação web e retornam JSON padronizado. Erros de validação ou JSON malformado são tratados globalmente pelo `ApiExceptionHandler` (`400 Bad Request`), enquanto recursos inexistentes retornam `404 Not Found`.

### Autenticação no Cliente REST (Postman / Insomnia / cURL)

Como o `SessaoInterceptor` protege tanto as telas quanto `/api/**`, realize primeiro uma requisição de login para obter o cookie `JSESSIONID`:

```bash
curl -i -c cookies.txt -X POST http://localhost:8081/login \
  -d "usuario=admin&senha=admin123"
```

Em seguida, envie o cookie nas chamadas da API (`-b cookies.txt`):

### Endpoints Disponíveis

| Método | Endpoint | Descrição | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/chamados` | Lista chamados com filtros opcionais (`busca`, `status`, `categoria`, `prioridade`, `inicio`, `fim`) | `200`, `401` |
| `GET` | `/api/chamados/{id}` | Busca os detalhes e o histórico completo de um chamado pelo ID | `200`, `401`, `404` |
| `GET` | `/api/chamados/indicadores` | Retorna os totais por status, alta prioridade e percentual de conclusão | `200`, `401` |
| `POST` | `/api/chamados` | Cria um novo chamado (`titulo`, `descricao`, `categoria`) | `201`, `400`, `401` |
| `PUT` | `/api/chamados/{id}` | Atualiza os dados cadastrais de um chamado existente | `200`, `400`, `401`, `404` |
| `PUT` | `/api/chamados/{id}/status` | Atualiza o status e registra nova observação no histórico | `200`, `400`, `401`, `404` |
| `DELETE` | `/api/chamados/{id}` | Exclui um chamado pelo ID | `204`, `401`, `404` |

### Exemplo de Payload (`POST /api/chamados`)

```json
{
  "titulo": "Projetor da sala 102 sem sinal",
  "descricao": "O equipamento liga, mas não reconhece a entrada HDMI do computador.",
  "categoria": "Suporte técnico"
}
```

---

## Refatorações e Boas Práticas Aplicadas (Clean Code)

Durante o desenvolvimento e a revisão final do código, foram aplicadas melhorias estruturais para aumentar a coesão e eliminar duplicidades:

1. **Centralização da Criação na `ChamadoFactory`:**
    * *Problema:* A instanciação de `Chamado` ficava dividida entre `ChamadoService` (que criava o objeto e preenchia campos básicos) e `ChamadoFactory` (que apenas completava prioridade e responsável), além de não registrar o evento inicial no histórico.
    * *Solução:* Refatoração para `ChamadoFactory.criar(ChamadoCadastroDTO)`, concentrando toda a montagem do estado inicial, definição de responsável/prioridade e criação do primeiro registro de histórico em um único ponto.
2. **Unificação da Busca por `#ID` na Camada de Serviço:**
    * *Problema:* A busca por `#ID` era tratada apenas via `fetch` adicional no JavaScript da listagem web, não funcionando na tela de relatórios nem na API REST.
    * *Solução:* Incorporação do tratamento de `#ID` diretamente no `ConsultaService` via JPA `Specification`, tornando o comportamento consistente em toda a aplicação e removendo código redundante do frontend.
3. **Propagação de Filtros nos Indicadores e Remoção de Código Morto:**
    * *Problema:* O endpoint `/indicadores` ignorava os parâmetros de período enviados pelos botões da tela, e existiam métodos não utilizados em `ChamadoService`/`ChamadoRepository` e um template HTML legado.
    * *Solução:* Vinculação de `FiltroChamadoDTO` em `RelatorioController.indicadores(...)` e remoção de métodos e arquivos obsoletos.
4. **Segurança na Exportação e Validação de Entrada:**
    * *Problema:* Campos iniciados com `=`, `+`, `-` ou `@` podem ser interpretados como fórmulas ao abrir arquivos CSV/Excel.
    * *Solução:* Sanitização automática na `ExportacaoService` e uso de DTOs específicos para cada operação (`ChamadoCadastroDTO`, `ChamadoEdicaoDTO`, `ChamadoAtualizacaoStatusDTO`), evitando exposição direta da entidade JPA.