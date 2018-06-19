package com

import cats.data.Writer

package object azavea extends Serializable {
  type Logged = Writer[Vector[String], Unit]
}
