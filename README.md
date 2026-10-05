# 📚 Sistema de Biblioteca

Aplicação desktop em **Java (Swing)** para gerenciar o acervo, os empréstimos e as reservas de uma biblioteca. Projeto da disciplina de **Programação Orientada a Objetos (UNIVAS)**, desenvolvido a partir do diagrama de classes (UML) fornecido pelo professor.

| | |
|---|---|
| **Arquitetura** | MVC (`modelo` · `visao` · `controle`) |
| **Interface** | Java Swing, com dois perfis: Bibliotecária e Usuário |
| **Persistência** | Arquivos `.csv` (UTF-8) na pasta `dados/`, sem banco de dados |
| **Relacionamento** | Composição: `Emprestimo` e `Reserva` guardam o `Livro` e o `Usuario` completos, não só os IDs |

## ▶️ Como executar

**Requisito:** JDK 17 ou superior (o código usa `switch` com `->`, disponível a partir do Java 14).

> ⚠️ Execute sempre a partir da **raiz do projeto**, a pasta onde estão `dados/` e `images/`. Os caminhos dos arquivos são relativos a ela.

**Pela IDE (IntelliJ, Eclipse, NetBeans, VS Code):** abra a pasta do projeto, marque `src/` como pasta de código-fonte e execute a classe `Main`.

**Pelo terminal:**

```bash
# Linux / macOS
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin Main
```

```bat
:: Windows (cmd)
dir /s /b src\*.java > fontes.txt
javac -encoding UTF-8 -d bin @fontes.txt
java -cp bin Main
```

Na primeira execução, se `dados/Bibliotecaria.csv` estiver vazio, o sistema cria a bibliotecária padrão **`admin`**.

## 🔑 Como entrar

Não há senha. Na tela inicial escolha o perfil (**Bibliotecária** ou **Usuário**) e selecione quem está acessando na lista.

| Perfil | O que pode fazer |
|---|---|
| **Bibliotecária** | CRUD completo de Livros, Usuários, Bibliotecárias, Empréstimos e Reservas; devoluções; relatórios |
| **Usuário** | Solicitar empréstimo, reservar livro, ver suas reservas e seu histórico de leitura |

## ✅ Funcionalidades

**CRUD (cadastrar, editar, excluir, listar)** para as cinco entidades:

| Entidade | Onde fica no menu da bibliotecária |
|---|---|
| Livro · Usuário · Bibliotecária | Cadastros & Edições (cadastrar, editar, excluir) · Relatórios (listar) |
| Empréstimo | Empréstimos (registrar, devolver, editar a data, excluir) · Relatórios (listar) |
| Reserva | Reservas (cadastrar, editar a data, excluir) · Relatórios (listar) |

**Relatórios:** Livros Disponíveis · Livros em Atraso (com dias de atraso) · Reservas Pendentes de efetivação.

## 📏 Regras de negócio

- **Prazo de empréstimo:** 7 dias.
- **Multa:** R$ 2,00 por dia de atraso. A devolução informa se foi **no prazo** ou **atrasada** e quantos dias.
- **Reserva por data:** feita para hoje ou data futura. Cada reserva ocupa 7 dias, então outra reserva do mesmo livro a menos de 7 dias de distância é recusada. Livro **emprestado** no momento também não pode ser reservado.
- **Empréstimo bloqueado** se o livro já está emprestado ou se está **reservado por outro usuário** dentro do prazo do empréstimo. Vale para a bibliotecária e para a solicitação do usuário.
- **Baixa automática da reserva:** ao emprestar para quem reservou, a reserva passa a `CONCLUIDA`.
- **Status do livro sempre coerente:** `EMPRESTADO` > `RESERVADO` > `DISPONIVEL`, recalculado a cada empréstimo, devolução, reserva ou exclusão.
- **Exclusão segura:** livro ou usuário com empréstimo ativo ou reserva ativa não podem ser excluídos. Ao excluir, o histórico deles também é removido, sem registros órfãos.

## 🗂️ Estrutura do projeto

```
SistemaBibliotecario/
├── UML/                 diagrama de classes do professor
├── docs/                prints das telas (usados neste README)
├── dados/               persistência em .csv
├── images/              biblioteca.jpg (fundo dos menus)
└── src/
    ├── Main.java
    ├── modelo/          Livro, Usuario, Bibliotecaria, Emprestimo, Reserva,
    │                    StatusLivro, StatusReserva
    ├── controle/        regras de negócio: LivroControle, UsuarioControle,
    │                    BibliotecariaControle, EmprestimoControle, ReservaControle
    ├── visao/
    │   ├── menus/       MenuInicial, MenuBibliotecaria, MenuUsuario
    │   │                + PainelFundo, PainelCartao, BotaoMenu (visual dos menus)
    │   └── telas/       cadastro, edição, exclusão e listagem
    └── util/            ManipuladorArquivos (leitura e escrita dos .csv)
```

**Fluxo MVC:** a *tela* (`visao`) captura o que o usuário digitou e chama o *controle*, que valida as regras e usa o `ManipuladorArquivos` para ler e gravar. O *modelo* guarda só os dados e o comportamento básico das entidades.

## 💾 Persistência

Cada entidade tem um arquivo em `dados/`, com campos separados por `;`.

| Arquivo | Campos |
|---|---|
| `Livro.csv` | id ; título ; autor ; status |
| `Usuario.csv` | id ; nome ; e-mail |
| `Bibliotecaria.csv` | id ; nome |
| `Emprestimo.csv` | id ; idLivro ; idUsuario ; data do empréstimo ; data da devolução ; ativo |
| `Reserva.csv` | id ; idLivro ; idUsuario ; data da reserva ; status |

No CSV ficam só os IDs; o `ManipuladorArquivos` reconstrói os objetos completos ao ler. Registros que apontam para livro ou usuário inexistente são ignorados.

## ⚠️ Limitações conhecidas

- Não há autenticação por senha; o acesso é por seleção de perfil.
- Os dados em CSV não suportam acesso simultâneo de várias instâncias do programa.
- Fechar qualquer janela pelo "X" encerra o programa inteiro.