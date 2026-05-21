# Letterbook — Gerenciador de Biblioteca Pessoal

Projeto semestral — Qualidade de Software (2026.1).  
Stack: **Spring Boot 3.3 + Java 21 + MongoDB 7/8 + JWT + HTML/CSS/JS puro**.

> Regra do edital: **proibido o uso de Mockito** — todos os testes de integração rodam com MongoDB real via **Testcontainers** e o ViaCEP é simulado com **WireMock + VCR (JSON gravado)**.

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot)
![MongoDB](https://img.shields.io/badge/MongoDB-8.3-green?logo=mongodb)
![Build](https://img.shields.io/badge/build-passing-brightgreen)

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

```txt
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
mvn spring-boot:run
```

A API sobe em:

```txt
http://localhost:8080
```

Endpoints públicos:
- `/api/auth/**`
- `/api/cep/**`

Os demais exigem:

```txt
Authorization: Bearer <jwt>
```

### 3.4 Rodar o frontend

Como o frontend é HTML estático, abra um servidor simples:

```powershell
cd frontend
python -m http.server 5500
# ou:
npx serve -l 5500
```

Acesse:

```txt
http://localhost:5500
```

Crie uma conta na tela **Cadastre-se**.

> **Não abra o `index.html` direto pelo `file://`** — o navegador bloqueia o `fetch` para `localhost:8080` por CORS quando o origin é `null`.

---

## 4. Testes e cobertura

```powershell
cd backend
mvn clean verify
```

Isto:

1. Compila com Java 21.
2. Sobe um MongoDB efêmero via **Testcontainers** para os testes de integração.
3. Roda **WireMock** local servindo as gravações em `src/test/resources/vcr/`.
4. Gera `target/site/jacoco/index.html`.
5. Executa as validações do JaCoCo configuradas no projeto.

### Tipos de teste presentes

- **Unitário Caixa Branca** — `EmailValidatorTest`, `PasswordPolicyTest`, `JwtServiceTest`.
- **Parametrizado** — `@ParameterizedTest` + `@ValueSource`.
- **Integração com Testcontainers** — `AuthControllerIT`, `BookControllerIT`.
- **VCR / API externa** — `CepServiceVcrTest`.
- **Caixa Preta / E2E / Controller** — validação de status HTTP, JSON e persistência real.

### Verificações típicas

```powershell
# Build completo
mvn clean verify

# Abrir cobertura
start target/site/jacoco/index.html

# Confirmar ausência de Mockito
findstr /S /I "mockito @MockBean Mockito.mock" src
```

---

## 5. Pipeline (GitHub Actions + SonarCloud)

- `.github/workflows/ci.yml` roda em **Ubuntu + Java 21**
- executa `mvn clean verify`
- publica relatório JaCoCo
- executa integração SonarCloud

Arquivo:

```txt
.github/workflows/ci.yml
```

Relatório JaCoCo:

```txt
backend/target/site/jacoco/jacoco.xml
```

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
| `Credenciais inválidas` | Usuário inexistente ou Mongo desligado | Cadastre-se primeiro e confirme MongoDB |
| `Failed to start container` | Docker desligado | Abra Docker Desktop |
| Java version error | `JAVA_HOME` incorreto | Configurar Java 21 |
| Frontend não chama API | Aberto via `file://` | Use `python -m http.server` |
| CORS no `/api/cep` | Frontend chama backend corretamente | Comportamento esperado |