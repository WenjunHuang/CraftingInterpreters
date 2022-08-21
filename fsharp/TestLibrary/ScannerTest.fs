module Tests

open Xunit
open FsUnit.Xunit
open com.github.wenjunhuang.lox

[<Fact>]
let ``Scanner should scan empty source`` () =
    let scanner = Scanner("")
    let tokens = scanner.scanTokens ()
    tokens.Length |> should equal 1

[<Fact>]
let ``Scanner should scan all supported tokens`` () =
    let tokens =
        [ ("(", LEFT_PAREN)
          (")", RIGHT_PAREN)
          ("{", LEFT_BRACE)
          ("}", RIGHT_BRACE)
          (",", COMMA)
          (".", DOT)
          ("-", MINUS)
          ("+", PLUS)
          (";", SEMICOLON)
          ("/", SLASH)
          ("*", STAR)
          ("!", BANG)
          ("!=", BANG_EQUAL)
          ("=", EQUAL)
          ("==", EQUAL_EQUAL)
          (">", GREATER)
          (">=", GREATER_EQUAL)
          ("<", LESS)
          ("<=", LESS_EQUAL)
          ("abc", IDENTIFIER)
          ("\"abc\"", STRING)
          ("123.123", NUMBER)
          ("and", AND)
          ("class", CLASS)
          ("else", ELSE)
          ("false", FALSE)
          ("fun", FUN)
          ("for", FOR)
          ("if", IF)
          ("nil", NIL)
          ("or", OR)
          ("print", PRINT)
          ("return", RETURN)
          ("super", SUPER)
          ("this", THIS)
          ("true", TRUE)
          ("var", VAR)
          ("while", WHILE) ]

    let source = tokens |> Seq.map fst |> String.concat " "
    let scanner = Scanner(source)
    let scannedTokens = scanner.scanTokens ()

    scannedTokens.Length
    |> should equal (tokens.Length + 1)

    Seq.last scannedTokens
    |> fun x -> x.tokenType
    |> should equal (EOF)


    Seq.zip tokens scannedTokens
    |> Seq.map (fun ((_, tokenType), token) -> token.tokenType |> should equal tokenType)
