# TQS: Quality Assurance Manual

## 1. Gestão de Projeto

### 1.1 Papéis atribuídos
Team Coordinator (Leader)- Paulo Cunha
Product Owner- Tomás Hilário
QA Engineer- Rafael Ferreira
DevOps master- Diogo Duarte


---
### TODO
### 1.2 Backlog grooming e monitorização de progresso
> **Instruções para preencher:**  
> - Indicar ferramenta de gestão (Jira, Azure Boards, etc.)  
> - Definir periodicidade do grooming  
> - Definir critérios de “Ready for Dev”  
> - Explicar como o progresso é monitorizado (burndown, velocity, etc.)  
> - Descrever como os testes são ligados às user stories  

---

## 2. Gestão da Qualidade de Código

### 2.1 Política da Equipa para Uso de IA Generativa

A utilização de ferramentas de IA generativa (como ChatGPT, GitHub Copilot, entre outras) é permitida no projeto, desde que cumpra regras de segurança, qualidade e responsabilidade. O objetivo é melhorar a produtividade sem comprometer a integridade do código ou dos dados.

#### Objetivos da política
- Aumentar a eficiência na escrita de código, documentação e testes.  
- Garantir que todo o conteúdo gerado por IA cumpre os padrões de qualidade definidos.  
- Proteger a confidencialidade dos dados do projeto e dos clientes.  
- Assegurar que os developers mantêm compreensão total do código produzido.

#### Práticas Permitidas (DOs)
- Utilizar IA para:
  - gerar código boilerplate ou exemplos iniciais;
  - sugerir cenários de teste, casos de teste ou edge cases;
  - ajudar na escrita de documentação;
  - explorar alternativas de implementação para resolver problemas;
  - explicar conceitos técnicos, padrões ou frameworks.

- Usar IA como apoio à análise estática informal:
  - revisão de qualidade do código;
  - sugestões de refatorização;
  - identificação de potenciais riscos.

- Utilizar IA para gerar testes (JUnit, Mockito, RestAssured, Selenium, k6), desde que:
  - o QA valide os cenários;
  - os developers confirmem a exatidão do código;
  - a pipeline valide a qualidade (SonarQube + CI/CD).

#### Práticas Não Permitidas (DON’Ts)
- Inserir na IA qualquer tipo de dado confidencial, incluindo:
  - dados de utilizadores ou clientes;
  - credenciais, tokens ou secrets;
  - informação sensível do repositório ou da infraestrutura.

- Enviar para IA:
  - código completo de módulos sensíveis;
  - partes significativas do repositório que permitam inferir lógica crítica.

- Aceitar código gerado por IA sem:
  - revisão completa por parte do desenvolvedor responsável;
  - criação/execução de testes adequados;
  - validação pelo SonarQube;
  - conformidade com a Definition of Done.

#### Responsabilidade sobre Código Gerado por IA
- Todo o código sugerido ou gerado por IA é considerado código da equipa.
- A responsabilidade final pela sua qualidade, segurança e correta integração é sempre humana.
- Pull requests contendo código gerado por IA seguem:
  - checklist de PR definida no projeto;
  - quality gates do SonarQube;
  - validação por peer review.

#### Utilização de IA em QA
- A IA pode ser utilizada para:
  - gerar casos de teste manuais ou automáticos;
  - preparar cenários funcionais ou histórias de utilizador;
  - ajudar na identificação de riscos de qualidade;
  - sugerir suites de regressão ou testes exploratórios.

- O QA é responsável por validar completamente qualquer output da IA antes de o incorporar em XRay ou no test plan.

A política será revista quando a equipa ou o projeto evoluírem, ou quando surgirem novas necessidades técnicas.


---

### TODO
### 2.2 Guidelines para contribuidores

#### 2.2.1 Estilo de código
> **Instruções para preencher:**  
> - Referenciar guias de estilo (Google Style Guide, Airbnb, etc.)  
> - Definir convenções de nomes, estrutura de pastas, padrões de arquitetura  
> - Regras específicas para backend, frontend, testes, documentação  

#### 2.2.2 Code review
> **Instruções para preencher:**  
> - Definir quando um PR é obrigatório  
> - Número mínimo de revisões  
> - Checklist de revisão (testes, segurança, linting, legibilidade, etc.)  
> - Regras para abertura de PRs (tamanho, descrição, ligação a user stories)  
> - Regras para feedback construtivo  

---

### 2.3 Métricas de qualidade de código e dashboards

A qualidade do código é monitorizada principalmente através do SonarQube Cloud e dos relatórios produzidos pela pipeline CI/CD. Estas métricas garantem que o software mantém níveis consistentes de fiabilidade, manutenibilidade e segurança.

#### Ferramentas Utilizadas
- **SonarQube Cloud**
  - Executa análise estática automaticamente em cada commit.
  - Avalia duplicação, bugs, vulnerabilidades, code smells e cobertura.
  - Aplica quality gates configurados pelo projeto.

- **XRay (Jira)**
  - Fornece visibilidade sobre cobertura funcional (User Story Coverage).
  - Permite mapear testes a requisitos e acompanhar resultados por sprint ou release.


### TODO(Tem de ser atualizado)
#### Quality Gate (Inicial)
- **Cobertura mínima de código:** ≥ 60%  
- **Duplicação:** < 5%  
- **Critical issues:** 0 permitidas  
- **Análise de segurança:** nenhuma vulnerabilidade crítica

Estas regras são aplicadas automaticamente pela pipeline e impedem merge caso não sejam cumpridas.

#### Métricas Monitorizadas
- **Code Coverage (SonarQube Cloud)**
  - Inclui testes unitários e de integração.
  - Utilizado como indicador principal de robustez do código.
  - Threshold inicial: **60%**.

- **User Story Coverage (XRay)**
  - Avalia se cada user story tem testes associados.
  - Permite identificar rapidamente lacunas de validação funcional.

- **Code Duplication**
  - Medido pelo SonarQube.
  - Threshold inicial: **<5%** de duplicação em novo código.

- **Code Smells**
  - Utilizado para avaliar a manutenibilidade.
  - Expectativa: nenhum code smell com severidade alta em novo código.

- **Bugs & Vulnerabilities**
  - Reportados automaticamente pelo SonarQube.
  - Expectativa: zero bugs críticos e zero vulnerabilidades críticas antes de merge.

#### Dashboards de Qualidade
- **SonarQube Cloud Dashboard**
  - Mostra métricas de qualidade por módulo, branch, commit e PR.
  - Indicadores chave:
    - Coverage
    - Bugs
    - Vulnerabilidades
    - Code Smells
    - Duplicação
    - Technical Debt

- **XRay Dashboard (Jira)**
  - Mostra:
    - Taxa de execução de testes
    - Falhas por sprint
    - Cobertura de requisitos
    - Resultados de regressão

Estes dashboards permitem tomadas de decisão rápidas e tornam a qualidade visível para toda a equipa.

#### Integração com CI/CD
- As pipelines executam automaticamente:
  - testes unitários e de integração
  - análise estática SonarQube
  - recolha de cobertura
  - publicação de relatórios

- O merge é automaticamente bloqueado quando:
  - o quality gate falha
  - a cobertura é inferior a 60%
  - existe pelo menos 1 critical issue
  - a duplicação excede 5%

Esta abordagem garante que a qualidade é verificada continuamente e que problemas críticos nunca chegam à branch principal.


---

### TODO
## 3. Pipeline de Entrega Contínua (CI/CD)

### 3.1 Workflow de desenvolvimento

#### 3.1.1 Workflow de código (Git)
> **Instruções para preencher:**  
> - Especificar o modelo de branching (GitFlow, GitHub Flow, trunk-based…)  
> - Regras para nomenclatura de branches  
> - Descrever o ciclo de desenvolvimento habitual (criar branch, commit, PR, review, merge)  

#### 3.1.2 Definition of Done (DoD)
> **Instruções para preencher:**  
> - Definir critérios de completude  
> - Relacionar com testes, documentação, qualidade, revisão, deploy  

---

### 3.2 Pipeline CI/CD e ferramentas
> **Instruções para preencher:**  
> - Indicar ferramenta CI (GitHub Actions, GitLab CI, Azure DevOps…)  
> - Listar passos da pipeline de CI (lint, build, testes, análise estática…)  
> - Explicar pipeline de CD (ambientes, triggers, regras de deploy)  

---

### 3.3 Observabilidade do sistema
> **Instruções para preencher:**  
> - Definir logging (níveis, formato, ferramentas)  
> - Definir recolha de métricas  
> - Indicar ferramentas de monitorização (Grafana, Prometheus, ELK…)  
> - Definir alertas e critérios de severidade  
> - Definir tracing distribuído se aplicável  

---

### 3.4 Repositório de artefactos (opcional)
> **Instruções para preencher:**  
> - Indicar onde os artefactos são armazenados (Nexus, Artifactory…)  
> - Descrever naming conventions  
> - Explicar política de retenção ou cleanup  

---

## 4. Testes de Software (Continuous Testing)

A estratégia de testes do projeto segue o princípio de validação contínua, garantindo que cada alteração de código é automaticamente verificada através de testes unitários, integração, funcionais e de performance. A gestão dos testes é realizada com XRay, enquanto a execução automática ocorre através da pipeline CI/CD.

---

### 4.1 Estratégia Global de Testes

A estratégia de testes segue tdd (test driven development) num modelo em camadas:

- **Testes Unitários**  
  Implementados com **JUnit** e **Mockito**, validam a lógica de negócio de forma isolada.  
  São executados automaticamente em cada push.

- **Testes de Integração**  
  Implementados com **RestAssured** e base de dados **PostgreSQL**.  
  Validam a interação entre componentes e o comportamento das APIs.

- **Testes Funcionais (End-to-End)**  
  Baseados em **Cucumber (BDD)** com **Selenium WebDriver** para simular a utilização real do sistema.  
  Validam fluxos completos do ponto de vista do utilizador.

- **Testes de Performance**  
  Executados com **k6**, asseguram que o sistema mantém níveis aceitáveis de desempenho sob carga.

- **Testes de Regressão**  
  O conjunto de testes existente (unitários, integração e funcionais) serve como suite de regressão.  
  É executado automaticamente em merge requests.

- **Testes Exploratórios**  
  Realizados manualmente em cada sprint para descobrir comportamentos inesperados, especialmente em funcionalidades novas ou sensíveis.

Todos os testes aplicáveis são geridos e reportados em **XRay**, permitindo rastreabilidade completa até às user stories.

---

### 4.2 Testes de Aceitação e ATDD

- Os testes de aceitação seguem o modelo **BDD**, utilizando **Cucumber** para descrever comportamentos da aplicação de forma legível.  
- Estes testes são executados com **Selenium WebDriver** para validar fluxos completos.  
- Cada teste de aceitação:
  - É mapeado a uma user story no **Jira/XRay**  
  - Verifica os critérios de aceitação da história  
  - Contribui para a cobertura funcional da release

Sempre que possível, testes de aceitação são automatizados e integrados na pipeline CI/CD.

---

### 4.3 Testes de Programador (Unitários e Integração)

#### Testes Unitários
- Implementados com **JUnit** e **Mockito**.  
- Focam-se na lógica interna de métodos e classes.  
- Devem cobrir casos positivos, negativos e edge cases.  
- Contribuem para a cobertura mínima de 60%.

#### Testes de Integração
- Implementados com **RestAssured** usando uma base de dados **PostgreSQL**.  
- Validam endpoints, fluxos API e interação com a camada de dados.  
- São executados em ambiente controlado pela pipeline CI.  
- Devem verificar comportamento normal, erros esperados e consistência dos dados.


---
### TODO(precisa de ser revisado)
### 4.4 Testes Exploratórios

- Realizados **manualmente** em cada sprint.  
- Focados em novas funcionalidades, alterações estruturais e áreas de maior risco.  
- O objetivo é identificar comportamentos inesperados que automatismos não captam.  
- Defeitos encontrados são registados no **Jira** e ligados às respetivas user stories.

Este tipo de teste complementa a automação existente e aumenta a confiança na release.

---

### 4.5 Testes Não Funcionais e Atributos de Arquitetura

#### Testes de Performance
- Executados utilizando **k6**.  
- São utilizados para avaliar:
  - tempo de resposta  
  - throughput  
  - estabilidade sob carga  
- Podem ser executados em fases chave do desenvolvimento ou antes de releases importantes.

---

### Integração da Estratégia com CI/CD

- Todos os testes são executados automaticamente pela pipeline CI/CD.  
- **Merge Requests são bloqueadas se:**
  - houver falhas em testes unitários ou de integração  
  - falharem testes funcionais automatizados  
  - a cobertura ficar abaixo de 60%  
  - o quality gate do SonarQube falhar  

Esta abordagem assegura que a qualidade é validada continuamente e que regressões não chegam à branch principal.
