# Letterbook — Gerenciador de Biblioteca Pessoal

Projeto semestral — Qualidade de Software (2026.1).
Stack: **Spring Boot 3.3 + Java 21 + MongoDB 7/8 + JWT + HTML/CSS/JS puro**.

> Regra do edital: **proibido o uso de Mockito** — todos os testes de integração rodam com MongoDB real via **Testcontainers** e o ViaCEP é simulado com **WireMock + VCR (JSON gravado)**.

---

## 1. Pré-requisitos (Windows 10/11)

| Ferramenta | Versão usada |
|---|---|
| JDK | **21.0.8** (configure `JAVA_HOME` apontando para o 21, NÃO o 25) |
| Maven | 3.9.11 |
| MongoDB | 8.3.2 rodando em `localhost:27017` |
| Docker Desktop | necessário **apenas para rodar os testes** (Testcontainers) |
| Node.js | 24.x (apenas se quiser servir o frontend com `npx serve`) |
| Git | 2.51+ |

Confirme o Java 21:
```powershell
java -version
# deve mostrar "21.0.8"
```
Se aparecer Java 25, ajuste `JAVA_HOME` e `Path` no Windows.

---

## 2. Estrutura

```
letterbook/
├── backend/                # Spring Boot
│   ├── pom.xml
│   └── src/{main,test}/...
├── frontend/               # HTML + CSS + JS puro
│   ├── index.html
│   ├── styles.css
│   └── app.js
├── .github/workflows/ci.yml
├── sonar-project.properties
├── RTM.md
└── README.md
```

---

## 3. Rodar localmente (passo a passo)

### 3.1 Subir o MongoDB
Confirme que o serviço **MongoDB** está rodando em `localhost:27017` (Serviços do Windows → MongoDB Server).

### 3.2 Configurar variáveis de ambiente (PowerShell)
```powershell
$env:MONGODB_URI = "mongodb://localhost:27017/letterbook"
$env:JWT_SECRET  = "troque-este-segredo-base64-com-pelo-menos-256-bits-em-producao-XX"
```

### 3.3 Rodar o backend
```powershell
cd backend
.\mvnw.cmd spring-boot:run
# ou: mvn spring-boot:run
```
A API sobe em `http://localhost:8080`. Endpoints públicos: `/api/auth/**` e `/api/cep/**`. Os demais exigem `Authorization: Bearer <jwt>`.

### 3.4 Rodar o frontend
Como o frontend é HTML estático, abra um servidor simples:
```powershell
cd frontend
python -m http.server 5500
# ou:  npx serve -l 5500
```
Acesse `http://localhost:5500`. Crie uma conta na tela **Cadastre-se** (senha mínima 8 caracteres, com letra e número).

> **Não abra o `index.html` direto pelo `file://`** — o navegador bloqueia o `fetch` para `localhost:8080` por CORS quando o origin é `null`.

---

## 4. Testes e cobertura (o que o professor abre)

```powershell
cd backend
mvn clean verify
```

Isto:
1. Compila com Java 21.
2. Sobe um MongoDB efêmero via **Testcontainers** para os ITs (precisa do Docker Desktop ligado).
3. Roda **WireMock** local servindo as gravações em `src/test/resources/vcr/` para os testes do ViaCEP.
4. Gera `target/site/jacoco/index.html`.
5. **Falha o build** se cobertura de linhas < 80% ou branches < 70% (regra do JaCoCo `check`).

### Tipos de teste presentes (mapeados no `RTM.md`)
- **Unitário Caixa Branca** — `EmailValidatorTest`, `PasswordPolicyTest`, `JwtServiceTest`.
- **Parametrizado** — `@ParameterizedTest` + `@ValueSource` em `EmailValidatorTest` e `PasswordPolicyTest`.
- **Integração com Testcontainers** — `AuthControllerIT`, `BookControllerIT` (estendem `AbstractMongoIT`, sobem Mongo real e batem nos endpoints com `TestRestTemplate`).
- **VCR / API externa** — `CepServiceVcrTest` (WireMock + JSON gravado, sem Mockito).
- **Caixa Preta / E2E / Controller** — todos os `*IT` validam status HTTP, corpo JSON e estado real do banco.

### Verificações típicas da banca
```powershell
# 1. Build verde
mvn clean verify

# 2. Cobertura >= 80% linhas
start target/site/jacoco/index.html

# 3. Nenhum mock no projeto (regra do edital)
findstr /S /I "mockito @MockBean Mockito.mock" src
# saída esperada: vazia
```

---

## 5. Pipeline (GitHub Actions + SonarCloud)

- `.github/workflows/ci.yml` roda em **Ubuntu + Java 21**, executa `mvn clean verify`, publica o relatório do JaCoCo como artifact e dispara `mvn sonar:sonar` se o segredo `SONAR_TOKEN` estiver configurado.
- `sonar-project.properties` aponta `sonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml`.

Para ativar Sonar:
1. Crie a organização/projeto em https://sonarcloud.io com a key `letterbook`.
2. Em **Settings → Secrets and variables → Actions** adicione `SONAR_TOKEN`.

---

## 6. Endpoints principais

| Método | Path | Auth | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | público | Cadastro |
| POST | `/api/auth/login` | público | Login → JWT |
| GET | `/api/cep/{cep}` | público | Consulta ViaCEP |
| GET | `/api/users/me` | JWT | Perfil |
| PUT | `/api/users/me` | JWT | Atualiza nome + endereço |
| GET | `/api/books?search=` | JWT | Lista (filtra) |
| POST | `/api/books` | JWT | Cria |
| PUT | `/api/books/{id}` | JWT | Atualiza |
| DELETE | `/api/books/{id}` | JWT | Remove |

---

## 7. Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| `Credenciais inválidas` no login | Sem usuário cadastrado **ou** MongoDB não está ligado | Cadastre-se primeiro; confirme `mongosh` conecta em 27017 |
| `Failed to start container` nos testes | Docker Desktop desligado | Abra o Docker antes de `mvn verify` |
| Java version error | `JAVA_HOME` apontando para JDK 25 | Repoint para JDK 21.0.8 |
| Frontend não chama API | Aberto via `file://` | Use `python -m http.server` |
| CORS no `/api/cep` | OK, o frontend já chama o **backend**, não o ViaCEP direto | — |
