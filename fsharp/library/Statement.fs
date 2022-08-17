namespace com.github.wenjunhuang.lox

type Statement =
    | Expr of expr: Expression
    | Var of name: Token * initializer: Expression option
    | If of condition: Expression * thenBranch: Statement * elseBranch: Statement option
    | Print of expr: Expression
    | Block of statements: array<Statement>
