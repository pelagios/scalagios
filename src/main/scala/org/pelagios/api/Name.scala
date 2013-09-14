package org.pelagios.api

/**
 * 'Name' model primitive.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class Name(labels: Seq[Label], altLabels: Seq[Label] = Seq.empty[Label]) { }