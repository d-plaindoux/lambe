# Lambë 

Strong typed functional programming and actor based language

# Functional Programming Paradigm

## Basic FP Concepts

### Function composition

```
def flip : [a,b,c] (a -> b -> c) -> (b -> a -> c)
def flip f = b a -> f a b

def (.) : [a,b,c] (b -> c) -> (a -> b) -> c
def (.) f g = x -> f $ g x

def (|>) : [a,b,c] (a -> b) -> (b -> c) -> c
def (|>) = flip (.)
```

### Monoïd

#### Definition

```
trait Monoid (a) {
    def mempty  : a
    def mappend : a -> a -> a 
}
```

#### Integers

```
def Monoid Int {
    def mempty = 0
    def mappend = (+)
}
```

#### Peano data type

```
def Peano : type
def Zero : Peano
def Succ : Peano -> Peano

trait Add (a) {
    def (+) : a -> a -> a
}

def Add Peano {
    def (+) Zero     p = p
    def (+) (Succ s) p = Succ (s + p)
}

def Monoid Peano with Add Peano {
    def mempty = Zero
    def mappend = (+)
}
```

### Trampoline definition

```
data Trampoline : type -> type
data Done : [a] a -> Trampoline a
data Next : [a] (Unit -> Trampoline a) -> Trampoline a
```
#### Runnable definition

```
trait Runnable (m:type->type) {
    def run : [a] m a -> a
}
```
#### Runnable Trampoline implementation

```
define Runnable Trampoline {
    def run (Done a) = a
    def run (Next f) = f unit run
}
```

#### Usage

```
trait Example {
    def fact : Int -> Int
}

define Example {
    def fact i = factTrampoline i 1 run

    def factTrampoline : Int -> Int -> Trampoline Int
    def factTrampoline 0 acc = Done acc
    def factTrampoline n acc = Next $ _ -> factTrampoline (n - 1) (n * acc)
}    
```

## Advanced FP Concepts

### Traits

``` 
trait Functor (m:type->type) {
  def pure : [a] a -> m a
  def fmap : [a,b] (a -> b) -> m a -> m b
}

trait Applicative (m:type->type) with Functor m {
  def (<*>) : [a,b] m (a -> b) -> m a -> m b
  def  lift2 : [a,b,c] (a -> b -> c) -> m a -> m b -> m c
  def (<$>) : [a,b] (a -> b) -> m a -> m b
}

define (m:type->type) Applicative m {
  def lift2 f = pure f <*>
  def (<$>) f = pure f <*>
}

trait Monad (m:type->type) with Applicative m {
    def (>>=) : [a,b] (a -> m b) -> m a -> m b
    def join  : [a] m (m a) -> m a
}

define (m:type->type) Monad m {
    def (>>=) f x = join $ fmap f x
}
```

### Data

```
data Option : type -> type
data None : [a] Option a
data Some : [a] a -> Option a
```

### Traits definition

```
define Functor Option {
  def pure = Some
  def fmap _ None     = None
  def fmap f (Some v) = Some (f v)
}

define Applicative Option {
  def (<*>) None     _ = None
  def (<*>) (Some f) v = f fmap v
}

define Monad Option {
  def join None     = None
  def join (Some v) = v
}
```

### Usage

```
fmap (1 +) $ pure 1                 // Some 2, of type Option Int 
pure (1 +) <*> $ pure 1             // Some 2, of type Option Int 
lift2 (+) (pure 1) (pure 1)         // Some 2, of type Option Int 
1 + <$> $ pure 1                    // Some 2, of type Option Int 
(i -> pure $ 1 + i) >>= $ pure 1    // Some 2, of type Option Int 
```

# Actor Paradigm

TODO

# Why Lambë

See [Lambë](http://tolkiengateway.net/wiki/Lambë) definition.

# License

Copyright 2018 D. Plaindoux.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
