# Lambë

A strong typed functional programming inspired by Haskell, OCaml and Rust.

## 0. Paradigms

Targeted programming language paradigms for the design of Lambë are:
- Trait based code organization
- coarse and fine grain self specification
- Trait implementation as first class citizen
- Higher-kinded-type
- Algebraic effects and handlers

## 1. Function 

### Definition

```
sig id       : a -> a
sig swap     : (a -> b -> c) -> b -> a -> c
sig compose  : (b -> c) -> (a -> b) -> a -> c
sig pipeline : (a -> b) -> (b -> c) -> a -> c
```

### Implementation

```
def id       = { _ } // equivalent to { a -> a }
def swap     = { f x y -> f y x }
def compose  = { f g x -> f $ g x }
def pipeline = swap compose
 ```

## 2. Data type

### Data type definition

```
data None
data Some a { v:a }
type Option a = None | Some a
```

### Data type implementation

```
impl for Option a {
    sig fold: self -> (None -> b) -> (Some a -> b) -> b

    def None.fold n _ = n self // self : None
    def Some.fold _ s = s self // self : Some a
}
```

In this implementation for `Option a` we use a type named `self`. In fact self denotes the type of the receiver which is `Option a` in this case defined thanks to the `for ...` declaration. Furthermore implementations are
given for each option data type i.e. None and Some.

### Data type in action

```
// for FP addicts
Some 1 fold { 0 } id

// for OO with FP flavor addicts
(Some 1).fold { 0 } id
```

## 3. Traits

### Trait definition

```
trait Functor (f:type->type) {
    sig fmap : self -> (a -> b) -> f b for f a
}
```

The `Functor` has a parametric type constructor `f` revealing the support of higher-kinded-types in the langage.

The `fmap` has a receiver called `self` and this receiver has the following type (given by the *for* directive): `f a`.

```
trait Applicative (f:type->type) with Functor f {
    sig pure   : a -> f a
    sig (<*>)  : self -> f a -> f b for f (a -> b)
    sig (<**>) : self -> f (a -> b) -> f b for f a

    def (<**>) a = a <*> self
}
```

Such *for* directive can be define at the trait level, method level or implementation level. If such directive is not expressed for a method it's a *static* method.

```
trait Monad (f:type->type) with Applicative f {
    sig join  : self -> f a for f (f a)
    sig (>>=) : self -> (a -> f b) -> f b for f a
    sig (=<<) : self -> f a -> f b for a -> f b

    def (>>=) f = self fmap f join
    def (=<<) a = a >>= self
}
```

Finally each method can be specified with a dedicated `self` type. As a conclusion a trait define a logical development unit.

### Trait implementation

```
impl Functor Option {
    def fmap f = self fold { None } { Some $ f _.v }
}

impl Applicative Option {
    def pure = Some
    def (<*>) a = self fold { None } { _.v fmap a }
}

impl Monad Option {
    def join = self fold { None } id    
}
```

### Trait implementation in action

```
// for FP addicts
Applicative Option pure 1 fmap (1+)     

// for OO with FP flavor addicts
((Applicative Option).pure 1).fmap (1+)
```

## 4. Examples

### Peanos' integer

```
data Zero
data Succ { v:Peano }
type Peano = Zero | Succ

trait Adder a for a {
    sig (+) : self -> self -> self
}

impl Adder Peano {
    def Zero.(+) a = a
    def Succ.(+) a = Succ (self v + a)
}
```

```
Succ Zero + $ Succ Zero)
```

### if/then/else DSL

WIP: Deferred capability i.e. Lazy | see connection with Algebraic Effect

```
data if {
    cond : -> bool // Deferred
}

data then a {
    if   : -> bool // Deferred
    then : -> a    // Deferred
}

impl for if {
    sig then : self -> (-> a) -> then a

    def then t = then self.cond t
}

impl for then a {
    // Deferred is finally Evaluated
    sig else : self -> (-> a) -> a

    def else f = self if fold (self then) f
}

// if (a > 0) then (a-1) else a  : int
// if (a > 0) then (a-1) else    : (-> int) -> int
// if (a > 0) then (a-1)         : then int
// if (a > 0) then               : (-> a) -> then a
// if (a > 0)                    : if
// if                            : (-> bool) -> if
```

### Collection builder

#### Collection builder Data

```
data CollectionBuilder b a {
    unbox : b
    add   : a -> CollectionBuilder b
}
```

#### Collection builder trait

```
trait OpenedCollection b a {
    sig ([)   : self -> a -> ClosableCollection b a
    sig empty : self -> b
}

trait ClosableCollection b a {
    sig (,) : self -> a -> ClosableCollection b a
    sig (]) : self -> b
}
```

#### Collection builder implementation

```
impl OpenedCollection b a for CollectionBuilder b a {
    def ([) a = self add a
    def empty = this unbox
}

impl ClosableCollection b a for CollectionBuilder b a {
    def (,) a = self add a
    def (])   = self unbox
}
```

#### The list builder

```
data Nil
data Cons a { h:a t:(List a) }
type List a = Nil | Cons a

sig List : (a:type) -> OpenedCollection (List a) a
def List _ =
    let builder l = CollectionBuilder l { builder $ Cons _ l } in
    	builder Nil
```

### The List builder in action

```
List int       : OpenedCollection (List int) int
List int [     : int -> ClosableCollection (List int) int
List int [1    : ClosableCollection (List int) int
List int [1,   : int -> ClosableCollection (List int) int
List int [1,2  : ClosableCollection (List int) int
List int [1,2] : List int
```

## 5. Modular system based on files

### File as trait

Each file containing Lambë code is a trait definition. For instance
a file named `list` can be defined by:
```
data Nil
data Cons a { h:a t:(List a) }
type List a = Nil | Cons a

sig (::) : a -> List a -> List a
def (::) = Cons

// :: 1 Nil
```

This file content is in fact similar to the trait:
```
trait list {
    data Nil
    data Cons a { h:a t:(List a) }
    type List a = Nil | Cons a

    sig (::) : a -> List a -> List a
    def (::) = Cons
}
```

This implies the capability to use list as a type elsewhere in the code
but also the capability to define trait, type etc. in a trait or it's
implementation.

### Generalising trait approach

If a file is a trait we can also reuse the `for` directive for each function.
```
trait list {
    data Nil
    data Cons a { h:a t:(List a) }
    type List a = Nil | Cons a

    sig (::) : self -> List a -> List a for a
    def (::) = Cons self

    // 1 :: Nil == 1.(::) Nil
}
```

### Using trait

How this trait can be used in another file? Simple! Just provide an implementation.

#### `Global` trait implementation usage

```
impl list

sig isEmpty : self -> bool for List a
def Nil.isEmpty = true
def Cons.isEmpty = false
```

#### `Local` trait implementation usage

Note: WIP

```
sig l : list
def l = impl list

sig isEmpty : self -> bool for l List a
def (l Nil).isEmpty = true
def (l Cons).isEmpty = false
```

### `Abstract` trait

Since a file is a trait it can also define signatures without implementation.
Therefore the definition should be done when the implementation is required.

For instance the `::` is specified but not defined:
```
data Nil
data Cons a { h:a t:(List a) }
type List a = Nil | Cons a

sig (::) : self -> List a -> List a for a
```

This trait then can be used but the function `::` implementation is mandatory.

```
impl list {
    def (::) = Cons self
}
```

## 6. Grammar

```
s0        ::= entity*

entity    ::= sig | def | data | type | trait | impl

sig       ::= "sig" dname ":" type for?
def       ::= "def" (self  ".")? dname  param* "=" expr
data      ::= "data" IDENT t_param* ("{" attr_elem* "}")?
type      ::= "type" IDENT t_param "=" type_expr
trait     ::= "trait" IDENT t_param* with* for? ("{" entity* "}")?
impl      ::= "impl" IDENT t_param* with* for? ("{" entity* "}")?

with      ::= "with" type_o
for       ::= "for" type_o

self      ::= IDENT
            | "(" IDENT* ")" // WIP

expr      ::= "{" (param+ "->")? expr "}"
            | "let" IDENT param* "=" expr "in" expr
            | param
            | native
            | "_"
            | expr expr
            | "(" expr ")"
            | dname | OPERATOR
            | expr "." dname
            | expr "with" ("IDENT "=" expr)+
            | expr "." IDENT
            | impl

type_expr ::= type_i "->" type_expr
            | "(" type_expr ")"
            | "->" type_expr
            | i_param
            | type_s
            | "self"
            | type_expr "|" type_expr

type_i    ::= i_param
            | type_o
            | "(" type_expr ")"

type_o    ::= "(" type_s ")"
            | o_param type_s?

attr_elem ::= IDENT ":" type

t_param   ::= i_param | o_param
i_param   ::= "(" IDENT ":" type ")"
o_param   ::= IDENT
param     ::= IDENT
dname     ::= IDENT | "(" OPERATOR ")"
native    ::= STRING | DOUBLE | INT | FLOAT | CHAR

IDENT     ::= [a-zA-Z][a-zA-Z0-9_$]* - KEYWORDS
KEYWORDS  ::= "sig"  | "def"   | "data"
            | "enum" | "trait" | "impl"
            | "type" | "with"  | "for"  
            | "let"  | "in"    | "self"

OPERATOR  ::= ([~$#?,;:@&!%><=+*/|_.^-]|\[|\])* - SYMBOLS
SYMBOLS   ::= "(" | ")" | "{" | "}" | "." | "->" | "_" | ":" | "." | "|"
```

# Why Lambë?

See [Lambë](http://tolkiengateway.net/wiki/Lambë) definition. May be also because it has the same prefix as lambda 😏

# License

Copyright 2019 D. Plaindoux.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
