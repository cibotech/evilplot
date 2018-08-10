package com.cibo.evilplot.interaction

import com.cibo.evilplot.interaction.Events.OptEvent
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object Events {
  type OptEvent = Option[() => Unit]

}

trait Interactive {

  val onClick: OptEvent
  val onMouseover: OptEvent
}

object EventCodec {
  implicit val clickEncoder: Encoder[OptEvent] = new Encoder[OptEvent] {
    def apply(a: OptEvent): Json = Json.Null
  }

  implicit val clickDecoder: Decoder[OptEvent] = new Decoder[OptEvent] {
    def apply(c: HCursor): Result[OptEvent] = Right(None)
  }
}
