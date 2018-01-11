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

trait TypeParser extends CoreParser with Coercions {

  def simpleTypeExpression: Parser[TypeAst] =
    positioned((Tokens.$type | identifier) ^^ TypeIdentifier | ("(" ~> typeExpression <~ ")"))

  def appliedTypeExpression: Parser[TypeAst] =
    positioned(simpleTypeExpression ~ appliedTypeExpression.? ^^ {
      case leftTypeExpression ~ None => leftTypeExpression
      case leftTypeExpression ~ Some(rightTypeExpression) => TypeApplication(leftTypeExpression, rightTypeExpression)
    })

  def funTypeExpression: Parser[TypeAst] =
    positioned(appliedTypeExpression ~ ("->" ~> typeExpression) ^^ {
      case leftTypeExpression ~ rightTypeExpression => TypeAbstraction(leftTypeExpression, rightTypeExpression)
    })

  def typeExpression: Parser[TypeAst] =
    funTypeExpression | appliedTypeExpression
}