# CI/CD — GitHub Actions + JaCoCo + SonarCloud

Pipeline já configurado em `.github/workflows/ci.yml`. A cada push/PR em `main`/`develop`/`master`:

1. Compila o backend (`./mvnw clean verify`)
2. Roda testes JUnit
3. Gera relatório de cobertura **JaCoCo** (`backend/target/site/jacoco/`)
4. Faz upload do relatório como artefato do workflow
5. Envia análise para o **SonarCloud** (se `SONAR_TOKEN` estiver configurado)

## 1. JaCoCo (já pronto)

Plugin configurado no `backend/pom.xml`. Para gerar localmente:

```bash
cd backend
./mvnw clean verify
# abrir: target/site/jacoco/index.html
```

Mínimos exigidos: **80% linhas** e **70% branches** (`<jacoco.line.min>` / `<jacoco.branch.min>` no `pom.xml`). Build falha se ficar abaixo.

## 2. SonarCloud — configuração única

1. Acesse https://sonarcloud.io e faça login com GitHub.
2. **+** → **Analyze new project** → selecione o repositório.
3. Use estes valores (ou ajuste em `sonar-project.properties` e no workflow):
   - **Organization key:** `letterbook`
   - **Project key:** `letterbook`
4. Em **Administration → Analysis Method**, **desative** "Automatic Analysis" (vamos usar o CI).
5. Em **My Account → Security**, gere um token (nome: `GH_ACTIONS`) e copie.

## 3. Secret no GitHub

No repositório: **Settings → Secrets and variables → Actions → New repository secret**

| Nome          | Valor                          |
|---------------|--------------------------------|
| `SONAR_TOKEN` | token gerado no SonarCloud     |

Pronto. Próximo push dispara o pipeline e o resultado aparece em:
`https://sonarcloud.io/project/overview?id=letterbook`

## 4. Badges (opcional)

Cole no topo do `README.md` trocando `SUA_ORG`:

```md
![CI](https://github.com/SUA_ORG/letterbook/actions/workflows/ci.yml/badge.svg)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=letterbook&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=letterbook)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=letterbook&metric=coverage)](https://sonarcloud.io/summary/new_code?id=letterbook)
```
