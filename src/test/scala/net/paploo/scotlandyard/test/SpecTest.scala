package net.paploo.scotlandyard.test

import org.scalatest.{BeforeAndAfterEach, FunSpec, Matchers, PrivateMethodTester}

abstract class SpecTest extends FunSpec with Matchers with BeforeAndAfterEach with PrivateMethodTester
