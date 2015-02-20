package org.pelagios.api.gazetteer.patch

class PatchConfig private(val geometryStrategy: PatchStrategy.Value, val namesStrategy: PatchStrategy.Value, val propagatePatch: Boolean) {
    
  def geometry(strategy: PatchStrategy.Value): PatchConfig =
    new PatchConfig(strategy, namesStrategy, propagatePatch)
  
  def names(strategy: PatchStrategy.Value): PatchConfig =
    new PatchConfig(geometryStrategy, strategy, propagatePatch)
  
  def propagate(propagate: Boolean): PatchConfig =
    new PatchConfig(geometryStrategy, namesStrategy, propagate)
  
}

object PatchConfig {
  
  def apply() = new PatchConfig(PatchStrategy.REPLACE, PatchStrategy.APPEND, true)
  
}

object PatchStrategy extends Enumeration {
  
  val REPLACE = Value("REPLACE")
  
  val APPEND = Value("APPEND")
  
}