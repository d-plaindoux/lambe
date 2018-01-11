/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package org.lambe.lang.syntax

import scala.util.parsing.input.Positional

trait Coercions {
  implicit def toType(a: String): TypeAst = TypeIdentifier(a)

  implicit def toExpression(a: String): ExpressionAst = ExpressionIdentifier(a)

  implicit def toPattern(a: String): PatternAst = PatternIdentifier(a)
}

object TypeDef {
  type Generics = List[(String, TypeAst)]
  type Parameters = List[TypeAst]
}

// ---------------------------------------------------------------------------------------------------------------------

trait TypeAst extends Positional

case class TypeIdentifier(name: String) extends TypeAst

case class TypeApplication(left: TypeAst, right: TypeAst) extends TypeAst

case class TypeAbstraction(left: TypeAst, right: TypeAst) extends TypeAst

// ---------------------------------------------------------------------------------------------------------------------

trait PatternAst extends Positional

case class PatternIdentifier(name: String) extends PatternAst

case class PatternApplication(left: PatternAst, right: PatternAst) extends PatternAst

// ---------------------------------------------------------------------------------------------------------------------

trait ExpressionAst extends Positional

case class ExpressionIdentifier(name: String) extends ExpressionAst

case class ExpressionApplication(left: ExpressionAst, right: ExpressionAst) extends ExpressionAst

case class ExpressionAbstraction(left: PatternAst, right: ExpressionAst) extends ExpressionAst

case class ExpressionLet(binding: PatternAst, value: ExpressionAst, body: ExpressionAst) extends ExpressionAst

// ---------------------------------------------------------------------------------------------------------------------

trait ValueAst extends Positional

case class ValueType(name: String, generics: TypeDef.Generics, spec: TypeAst) extends ValueAst

case class ValueExpression(name: String, selfPattern: Option[PatternAst], spec: ExpressionAst) extends ValueAst

// ---------------------------------------------------------------------------------------------------------------------

trait EntityAst extends Positional

case class DataEntity(name: String, generics: TypeDef.Generics, spec: TypeAst) extends EntityAst

case class TraitEntity(name: String, generics: TypeDef.Generics, spec: List[ValueType]) extends EntityAst

case class DefineEntity(generics: TypeDef.Generics, model: TypeAst, data: TypeAst, spec: List[ValueExpression]) extends EntityAst