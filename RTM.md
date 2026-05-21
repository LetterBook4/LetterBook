# RTM - Matriz de Rastreabilidade de Requisitos

| ID | Requisito Funcional | Camada | Teste(s) automatizado(s) | Tipo de teste |
|---|---|---|---|---|
| RF-01 | Cadastrar livro | `BookController.create` | `BookControllerIT#crudFullCycle` | Integração / Caixa Preta / Testcontainers |
| RF-02 | Listar livros do usuário | `BookController.list` | `BookControllerIT#crudFullCycle` | Integração / Caixa Preta |
| RF-03 | Editar livro | `BookController.update` | `BookControllerIT#crudFullCycle` | Integração / Caixa Preta |
| RF-04 | Excluir livro | `BookController.delete` | `BookControllerIT#crudFullCycle` | Integração / Caixa Preta |
| RF-05 | Buscar livros por título/autor | `BookController.list(search)` | `BookControllerIT#searchFiltersByTitleOrAuthor` | Integração / E2E |
| RF-06 | Cadastro de usuário | `AuthController.register` | `AuthControllerIT#registerThenLoginReturnsJwt` | Integração / Testcontainers |
| RF-07 | Login com JWT | `AuthController.login` | `AuthControllerIT#registerThenLoginReturnsJwt` | Integração / E2E |
| RF-08 | E-mail único no cadastro | `AuthService.register` | `AuthControllerIT#duplicateEmailReturns409` | Integração |
| RF-09 | Validação de e-mail | `EmailValidator` | `EmailValidatorTest` | Unitário Parametrizado / Caixa Branca |
| RF-10 | Política de senha forte | `PasswordPolicy` | `PasswordPolicyTest` | Unitário Parametrizado / Caixa Branca |
| RF-11 | Senha fraca rejeitada no cadastro | `AuthService.register` | `AuthControllerIT#invalidPasswordReturns400` | Integração |
| RF-12 | Filtro JWT bloqueia acessos não autenticados | `SecurityConfig` + `JwtAuthFilter` | `BookControllerIT#unauthenticatedRequestReturns401` | Integração / Segurança |
| RF-13 | Usuário só acessa os próprios livros | `BookService.getOwned` | `BookControllerIT#cannotAccessOtherUsersBook` | Integração / Caixa Preta |
| RF-14 | Geração/validação de token JWT | `JwtService` | `JwtServiceTest` | Unitário / Caixa Branca |
| RF-15 | Consulta de CEP (ViaCEP) | `CepService.lookup` | `CepServiceVcrTest#buscaCepValidoRetornaEndereco` | Integração API externa / WireMock / VCR |
| RF-16 | CEP inexistente retorna 404 | `CepService.lookup` | `CepServiceVcrTest#cepInexistenteLanca404` | VCR / Caixa Preta |
| RF-17 | CEP em formato inválido rejeitado | `CepService.lookup` | `CepServiceVcrTest#cepFormatoInvalidoLancaBadRequest` | Unitário / Validação |
| RF-18 | Editar perfil e endereço | `UserController.updateMe` | `AuthControllerIT` | Integração |

> Regra crítica do edital: **proibido o uso de Mockito/`@MockBean`**. Toda integração roda contra MongoDB real via **Testcontainers**, e a integração com ViaCEP usa **WireMock + JSON gravado (VCR)**.

---

# Diagramas de Sequência UML

## RF-01 — Cadastrar livro

```mermaid
sequenceDiagram
  participant U as Usuário
  participant F as Frontend
  participant BC as BookController
  participant BS as BookService
  participant BR as BookRepository
  participant M as MongoDB

  U->>F: preenche formulário
  F->>BC: POST /api/books
  BC->>BS: create(ownerId, dto)
  BS->>BR: save(book)
  BR->>M: persistência
  M-->>BR: livro salvo
  BR-->>BS: Book
  BS-->>BC: Book
  BC-->>F: 201 Created
```

## RF-02 — Listar livros

```mermaid
sequenceDiagram
  participant F as Frontend
  participant BC as BookController
  participant BS as BookService
  participant BR as BookRepository
  participant M as MongoDB

  F->>BC: GET /api/books
  BC->>BS: listMine(userId)
  BS->>BR: findByOwnerId(userId)
  BR->>M: consulta livros
  M-->>BR: lista
  BR-->>BS: livros
  BS-->>BC: livros
  BC-->>F: 200 OK
```

## RF-03 — Editar livro

```mermaid
sequenceDiagram
  participant F as Frontend
  participant BC as BookController
  participant BS as BookService
  participant BR as BookRepository
  participant M as MongoDB

  F->>BC: PUT /api/books/{id}
  BC->>BS: update(ownerId,id,dto)
  BS->>BR: findById(id)
  BR->>M: busca livro
  M-->>BR: livro
  BS->>BR: save(book atualizado)
  BR->>M: persistência
  M-->>BR: atualizado
  BR-->>BS: Book
  BS-->>BC: Book
  BC-->>F: 200 OK
```

## RF-04 — Excluir livro

```mermaid
sequenceDiagram
  participant F as Frontend
  participant BC as BookController
  participant BS as BookService
  participant BR as BookRepository
  participant M as MongoDB

  F->>BC: DELETE /api/books/{id}
  BC->>BS: delete(ownerId,id)
  BS->>BR: findById(id)
  BR->>M: busca livro
  M-->>BR: livro
  BS->>BR: deleteById(id)
  BR->>M: remove livro
  M-->>BR: OK
  BS-->>BC: sucesso
  BC-->>F: 204 No Content
```

## RF-05 — Busca de livros por título/autor

```mermaid
sequenceDiagram
  participant U as Usuário
  participant F as Frontend
  participant BC as BookController
  participant BS as BookService
  participant BR as BookRepository
  participant M as MongoDB

  U->>F: digita termo de busca
  F->>BC: GET /api/books?search=clean
  BC->>BS: search(userId, termo)
  BS->>BR: findByOwnerIdAndSearch(...)
  BR->>M: consulta filtrada
  M-->>BR: livros encontrados
  BR-->>BS: lista
  BS-->>BC: resultado
  BC-->>F: 200 OK + livros
```

## RF-06 — Cadastro de usuário

```mermaid
sequenceDiagram
  participant U as Usuário
  participant F as Frontend
  participant AC as AuthController
  participant AS as AuthService
  participant UR as UserRepository
  participant M as MongoDB

  U->>F: preenche cadastro
  F->>AC: POST /api/auth/register
  AC->>AS: register(data)
  AS->>UR: existsByEmail(email)
  UR->>M: consulta email
  M-->>UR: resultado
  AS->>UR: save(user)
  UR->>M: persiste usuário
  M-->>UR: usuário salvo
  UR-->>AS: User
  AS-->>AC: sucesso
  AC-->>F: 201 Created
```

## RF-07 — Login com JWT

```mermaid
sequenceDiagram
  participant U as Usuário
  participant F as Frontend
  participant AC as AuthController
  participant AS as AuthService
  participant UR as UserRepository
  participant JS as JwtService
  participant M as MongoDB

  U->>F: informa email e senha
  F->>AC: POST /api/auth/login
  AC->>AS: login(req)
  AS->>UR: findByEmail(email)
  UR->>M: consulta usuário
  M-->>UR: User
  UR-->>AS: User
  AS->>JS: generateToken(user)
  JS-->>AS: JWT
  AS-->>AC: AuthResponse
  AC-->>F: 200 OK + token
```

## RF-08 — Validação de e-mail único

```mermaid
sequenceDiagram
  participant AC as AuthController
  participant AS as AuthService
  participant UR as UserRepository
  participant M as MongoDB

  AC->>AS: register(email)
  AS->>UR: existsByEmail(email)
  UR->>M: consulta email
  M-->>UR: email já existe
  UR-->>AS: true
  AS-->>AC: ConflictException
  AC-->>AC: retorna 409
```

## RF-09 — Validação de e-mail

```mermaid
sequenceDiagram
  participant T as Teste
  participant EV as EmailValidator

  T->>EV: isValid("email@teste.com")
  EV-->>T: true

  T->>EV: isValid("email-invalido")
  EV-->>T: false
```

## RF-10 — Política de senha forte

```mermaid
sequenceDiagram
  participant T as Teste
  participant PP as PasswordPolicy

  T->>PP: validate("Senha123")
  PP-->>T: válido

  T->>PP: validate("123")
  PP-->>T: inválido
```

## RF-11 — Rejeição de senha fraca

```mermaid
sequenceDiagram
  participant F as Frontend
  participant AC as AuthController
  participant AS as AuthService

  F->>AC: POST /register senha fraca
  AC->>AS: register()
  AS->>AS: validatePassword()
  AS-->>AC: BadRequestException
  AC-->>F: 400 Bad Request
```

## RF-12 — Bloqueio sem autenticação JWT

```mermaid
sequenceDiagram
  participant U as Usuário
  participant JF as JwtAuthFilter
  participant API as API

  U->>JF: request sem token
  JF->>JF: token ausente
  JF-->>API: bloqueia requisição
  API-->>U: 401 Unauthorized
```

## RF-13 — Usuário só acessa os próprios livros

```mermaid
sequenceDiagram
  participant F as Frontend
  participant BC as BookController
  participant BS as BookService
  participant BR as BookRepository

  F->>BC: GET /api/books/{id}
  BC->>BS: getOwned(userId,id)
  BS->>BR: findById(id)
  BR-->>BS: livro de outro usuário
  BS-->>BC: AccessDeniedException
  BC-->>F: 403 Forbidden
```

## RF-14 — Geração e validação JWT

```mermaid
sequenceDiagram
  participant JS as JwtService

  JS->>JS: generateToken(user)
  JS-->>JS: JWT assinado

  JS->>JS: validateToken(jwt)
  JS-->>JS: token válido
```

## RF-15 — Consulta de CEP

```mermaid
sequenceDiagram
  participant F as Frontend
  participant CC as CepController
  participant CS as CepService
  participant API as ViaCEP

  F->>CC: GET /api/cep/{cep}
  CC->>CS: lookup(cep)
  CS->>API: consulta ViaCEP
  API-->>CS: endereço
  CS-->>CC: CepResponse
  CC-->>F: 200 OK
```

## RF-16 — CEP inexistente

```mermaid
sequenceDiagram
  participant F as Frontend
  participant CS as CepService
  participant API as ViaCEP

  F->>CS: lookup(00000000)
  CS->>API: consulta CEP
  API-->>CS: erro=true
  CS-->>F: 404 Not Found
```

## RF-17 — CEP inválido

```mermaid
sequenceDiagram
  participant F as Frontend
  participant CS as CepService

  F->>CS: lookup("abc")
  CS->>CS: valida formato
  CS-->>F: 400 Bad Request
```

## RF-18 — Atualização de perfil

```mermaid
sequenceDiagram
  participant F as Frontend
  participant UC as UserController
  participant UR as UserRepository
  participant M as MongoDB

  F->>UC: PUT /api/users/me
  UC->>UR: save(user)
  UR->>M: persistência
  M-->>UR: usuário atualizado
  UR-->>UC: sucesso
  UC-->>F: 200 OK
```