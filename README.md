# Sistema de Biblioteca

Projeto acadêmico da disciplina de Programação Orientada a Objetos (UNIVAS),
desenvolvido a partir da UML da pasta `UML/`.

Aplicação desktop em Java (Swing) organizada no padrão **MVC**, com
persistência simples em arquivos `.csv` (UTF-8) e relacionamento entre classes
por **composição** (ex.: um `Emprestimo` guarda o objeto `Livro` e o objeto
`Usuario` completos, não apenas seus IDs).

## Estrutura

```
Biblioteca/
├── UML/                          // diagrama de classes do professor
├── dados/                        // persistência em .csv
├── images/biblioteca.jpg         // imagem de fundo dos menus
└── src/
    ├── Main.java
    ├── modelo/     Livro, Usuario, Bibliotecaria, Emprestimo, Reserva,
    │               StatusLivro, StatusReserva
    ├── controle/   LivroControle, UsuarioControle, BibliotecariaControle,
    │               EmprestimoControle, ReservaControle
    ├── visao/
    │   ├── menus/  MenuInicial, MenuBibliotecaria, MenuUsuario
    │   └── telas/  telas de cadastro, edição, exclusão e listagem
    └── util/       ManipuladorArquivos (leitura/escrita dos .csv)
```

## Como executar

1. Abra a pasta do projeto no IntelliJ/Eclipse/NetBeans/VS Code
   (a pasta de trabalho deve ser a raiz do projeto, onde estão `dados/` e `images/`).
2. Compile e execute a classe `Main` (pacote raiz).
3. Na primeira execução, `Main` cria a bibliotecária padrão (`admin`) caso
   `dados/Bibliotecaria.csv` esteja vazio.
4. Na tela inicial escolha o perfil **Bibliotecária** ou **Usuário**.

## Funcionalidades (requisitos do professor)

**CRUD completo** (cadastrar, editar, excluir, listar) para Usuário, Livro,
Bibliotecária, Empréstimo e Reserva, pelo menu da bibliotecária:

| Entidade | Cadastrar | Editar | Excluir | Listar |
|---|---|---|---|---|
| Usuário / Livro / Bibliotecária | Cadastros & Edições | Cadastros & Edições | Cadastros & Edições | Relatórios |
| Empréstimo | Operações > Registrar Empréstimo (bibliotecária) ou Solicitar Empréstimo (usuário) | Operações (data) | Operações | Relatórios |
| Reserva | Operações > Cadastrar Reserva (bibliotecária) ou Reservar Livro (usuário) | Operações (data) | Operações | Relatórios |

**Relatórios:** Livros Disponíveis, Livros em Atraso (com dias de atraso) e
Reservas Pendentes de efetivação.

## Regras de negócio

- **Prazo de empréstimo:** 7 dias.
- **Reserva por data:** a reserva é feita para uma data (hoje ou futura).
  - Bloqueia livro **emprestado** no momento.
  - Bloqueia livro **já reservado na data**: cada reserva ocupa o prazo de 7 dias,
    então outra reserva do mesmo livro a menos de 7 dias de distância é recusada.
- **Empréstimo:** bloqueia livro já emprestado e livro **reservado por outro
  usuário** dentro do prazo do empréstimo (a reserva cai antes de 7 dias).
  Vale para a bibliotecária e para a solicitação do usuário.
- **Baixa automática da reserva:** ao efetuar o empréstimo para o usuário que
  reservou, a reserva passa a `CONCLUIDA`.
- **Devolução:** o sistema informa se está **dentro do prazo** ou **atrasada** e a
  **quantidade de dias em atraso**, com a multa (R$ 2,00/dia de atraso).
- **Status do livro** sempre coerente: `EMPRESTADO` (empréstimo ativo) >
  `RESERVADO` (reserva ativa) > `DISPONIVEL`. É recalculado a cada empréstimo,
  devolução, reserva ou exclusão.
- **Exclusão segura:** livro ou usuário com empréstimo ativo **ou reserva ativa**
  não podem ser excluídos. Ao excluir, o histórico deles (empréstimos devolvidos e
  reservas encerradas) também é removido, sem deixar registros órfãos.
- O usuário consulta o **Histórico de Leitura** (empréstimos já devolvidos) no menu.

## Persistência

Todos os dados são salvos em `.csv` dentro de `dados/`. No CSV ficam gravados os
IDs; `ManipuladorArquivos` reconstrói os objetos completos ao ler (buscando cada
`Livro`/`Usuario` pelo ID). Registros que apontam para livro/usuário inexistente
são ignorados, e um empréstimo nunca aparece duplicado (vale o último registro do ID).
