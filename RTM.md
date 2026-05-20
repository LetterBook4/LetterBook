# RTM - Matriz de Rastreabilidade de Requisitos

| ID    | Requisito Funcional                          | Camada                          | Teste(s) automatizado(s)                                          | Tipo de teste                              |
|-------|----------------------------------------------|---------------------------------|-------------------------------------------------------------------|--------------------------------------------|
| RF-01 | Cadastrar livro                              | `BookController.create`         | `BookControllerIT#crudFullCycle`                                  | Integração / Caixa Preta / Testcontainers  |
| RF-02 | Listar livros do usuário                     | `BookController.list`           | `BookControllerIT#crudFullCycle`                                  | Integração / Caixa Preta                   |
| RF-03 | Editar livro                                 | `BookController.update`         | `BookControllerIT#crudFullCycle`                                  | Integração / Caixa Preta                   |
| RF-04 | Excluir livro                                | `BookController.delete`         | `BookControllerIT#crudFullCycle`                                  | Integração / Caixa Preta                   |
| RF-05 | Buscar livros por título/autor               | `BookController.list(search)`   | `BookControllerIT#searchFiltersByTitleOrAuthor`                   | Integração / E2E                           |
| RF-06 | Cadastro de usuário                          | `AuthController.register`       | `AuthControllerIT#registerThenLoginReturnsJwt`                    | Integração / Testcontainers                |
| RF-07 | Login com JWT                                | `AuthController.login`          | `AuthControllerIT#registerThenLoginReturnsJwt`                    | Integração / E2E                           |
| RF-08 | E-mail único no cadastro                     | `AuthService.register`          | `AuthControllerIT#duplicateEmailReturns409`                       | Integração                                 |
| RF-09 | Validação de e-mail                          | `EmailValidator`                | `EmailValidatorTest` (vários cenários)                            | Unitário Parametrizado / Caixa Branca      |
| RF-10 | Política de senha forte                      | `PasswordPolicy`                | `PasswordPolicyTest` (vários cenários)                            | Unitário Parametrizado / Caixa Branca      |
| RF-11 | Senha fraca rejeitada no cadastro            | `AuthService.register`          | `AuthControllerIT#invalidPasswordReturns400`                      | Integração                                 |
| RF-12 | Filtro JWT bloqueia acessos não autenticados | `SecurityConfig` + `JwtAuthFilter` | `BookControllerIT#unauthenticatedRequestReturns401`              | Integração / Segurança                     |
| RF-13 | Usuário só acessa os próprios livros         | `BookService.getOwned`          | `BookControllerIT#cannotAccessOtherUsersBook`                     | Integração / Caixa Preta                   |
| RF-14 | Geração/validação de token JWT               | `JwtService`                    | `JwtServiceTest`                                                  | Unitário / Caixa Branca                    |
| RF-15 | Consulta de CEP (ViaCEP)                     | `CepService.lookup`             | `CepServiceVcrTest#buscaCepValidoRetornaEndereco`                 | Integração com API externa via WireMock/VCR|
| RF-16 | CEP inexistente retorna 404                  | `CepService.lookup`             | `CepServiceVcrTest#cepInexistenteLanca404`                        | VCR / Caixa Preta                          |
| RF-17 | CEP em formato inválido rejeitado            | `CepService.lookup`             | `CepServiceVcrTest#cepFormatoInvalidoLancaBadRequest`             | Unitário / Validação                       |
| RF-18 | Editar perfil + endereço (com ViaCEP)        | `UserController.updateMe`       | `AuthControllerIT` (endereço persistido) + frontend `buscarCEP`   | Integração                                 |

> Regra crítica do edital: **proibido o uso de Mockito/`@MockBean`**. Toda integração roda contra MongoDB real via **Testcontainers**, e a integração com ViaCEP usa **WireMock + JSON gravado (VCR)** em `src/test/resources/vcr/`.

---

## Diagramas de Sequência (UML / Mermaid)

### RF-07 — Login com JWT

```mermaid
sequenceDiagram
  participant U as Usuário
  participant F as Frontend
  participant C as AuthController
  participant S as AuthService
  participant R as UserRepository
  participant J as JwtService
  participant M as MongoDB

  U->>F: preenche email + senha
  F->>C: POST /api/auth/login
  C->>S: login(req)
  S->>R: findByEmail(email)
  R->>M: query users
  M-->>R: User
  R-->>S: User
  S->>S: passwordEncoder.matches()
  S->>J: generate(userId, email)
  J-->>S: JWT
  S-->>C: AuthResponse(token, user)
  C-->>F: 200 OK + token
  F->>F: localStorage.setItem('lb_token')
```

### RF-15 — Consulta de CEP (ViaCEP)

```mermaid
sequenceDiagram
  participant F as Frontend
  participant CC as CepController
  participant CS as CepService
  participant V as ViaCEP API

  F->>CC: GET /api/cep/{cep}
  CC->>CS: lookup(cep)
  CS->>CS: sanitiza e valida 8 dígitos
  CS->>V: GET /ws/{cep}/json/
  V-->>CS: 200 {logradouro, bairro, ...}
  CS-->>CC: CepResponse
  CC-->>F: 200 OK
  F->>F: auto-preenche rua/bairro/cidade/UF
```

### RF-01..04 — CRUD de Livros (autenticado)

```mermaid
sequenceDiagram
  participant F as Frontend
  participant JF as JwtAuthFilter
  participant BC as BookController
  participant BS as BookService
  participant BR as BookRepository
  participant M as MongoDB

  F->>JF: Authorization: Bearer <token>
  JF->>JF: parseSubject -> userId
  JF->>BC: request com SecurityContext
  BC->>BS: create/update/delete/list
  BS->>BR: save / findByOwnerId / deleteById
  BR->>M: persistência
  M-->>BR: resultado
  BR-->>BS: Book(s)
  BS-->>BC: View
  BC-->>F: 200/201/204
```

### RF-13 — Autorização por dono

```mermaid
sequenceDiagram
  participant F as Frontend(outro usuário)
  participant BC as BookController
  participant BS as BookService
  participant BR as BookRepository

  F->>BC: GET /api/books/{id}
  BC->>BS: getOwned(userId, id)
  BS->>BR: findById(id)
  BR-->>BS: Book(ownerId != userId)
  BS-->>BC: AccessDeniedException
  BC-->>F: 403 Forbidden
```
