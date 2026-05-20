### RF-05 — Busca de livros por título/autor

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

### RF-06 — Cadastro de usuário

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

### RF-08 — Validação de e-mail único

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
  AS-->>AC: DuplicateEmailException
  AC-->>AC: retorna 409
```

### RF-09 — Validação de e-mail

```mermaid
sequenceDiagram
  participant T as Teste
  participant EV as EmailValidator

  T->>EV: isValid("email@teste.com")
  EV-->>T: true

  T->>EV: isValid("email-invalido")
  EV-->>T: false
```

### RF-10 — Política de senha forte

```mermaid
sequenceDiagram
  participant T as Teste
  participant PP as PasswordPolicy

  T->>PP: validate("Senha123")
  PP-->>T: válido

  T->>PP: validate("123")
  PP-->>T: inválido
```

### RF-11 — Rejeição de senha fraca

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

### RF-12 — Bloqueio sem autenticação JWT

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

### RF-14 — Geração e validação JWT

```mermaid
sequenceDiagram
  participant JS as JwtService

  JS->>JS: generateToken(user)
  JS-->>JS: JWT assinado

  JS->>JS: validateToken(jwt)
  JS-->>JS: token válido
```

### RF-16 — CEP inexistente

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

### RF-17 — CEP inválido

```mermaid
sequenceDiagram
  participant F as Frontend
  participant CS as CepService

  F->>CS: lookup("abc")
  CS->>CS: valida formato
  CS-->>F: 400 Bad Request
```

### RF-18 — Atualização de perfil

```mermaid
sequenceDiagram
  participant F as Frontend
  participant UC as UserController
  participant US as UserService
  participant UR as UserRepository
  participant M as MongoDB

  F->>UC: PUT /api/users/me
  UC->>US: updateProfile()
  US->>UR: save(user)
  UR->>M: persistência
  M-->>UR: usuário atualizado
  UR-->>US: sucesso
  US-->>UC: dados atualizados
  UC-->>F: 200 OK
```