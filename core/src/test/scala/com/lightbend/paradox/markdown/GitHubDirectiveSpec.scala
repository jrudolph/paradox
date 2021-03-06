/*
 * Copyright © 2015 - 2016 Lightbend, Inc. <http://www.lightbend.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.paradox.markdown

import com.lightbend.paradox.tree.Tree.Location

class GibHubDirectiveSpec extends MarkdownBaseSpec {

  implicit val context = writerContextWithProperties(
    "github.base_url" -> "https://github.com/lightbend/paradox/tree/v0.2.1")

  "GitHub directive" should "create links using configured base URL" in {
    markdown("@github[#1](#1)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/issues/1">#1</a></p>""")
  }

  it should "support 'github:' as an alternative name" in {
    markdown("@github:[#1](#1)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/issues/1">#1</a></p>""")
  }

  it should "retain whitespace before or after" in {
    markdown("The @github:[#1](#1) issue") shouldEqual
      html("""<p>The <a href="https://github.com/lightbend/paradox/issues/1">#1</a> issue</p>""")
  }

  it should "parse but ignore directive attributes" in {
    markdown("The @github:[#1](#1) { .github a=1 } issue") shouldEqual
      html("""<p>The <a href="https://github.com/lightbend/paradox/issues/1">#1</a> issue</p>""")
  }

  it should "handle issue links to other project" in {
    markdown("@github[akka/akka#1234](akka/akka#1234)") shouldEqual
      html("""<p><a href="https://github.com/akka/akka/issues/1234">akka/akka#1234</a></p>""")
  }

  it should "handle commits links to other project" in {
    markdown("@github[akka/akka@2da7b26b](akka/akka@2da7b26b)") shouldEqual
      html("""<p><a href="https://github.com/akka/akka/commit/2da7b26b">akka/akka@2da7b26b</a></p>""")
  }

  it should "handle tree links" in {
    markdown("@github[See build.sbt](/build.sbt)") shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/tree/v0.2.1/build.sbt">See build.sbt</a></p>""")
  }

  it should "handle tree links with automatic versioning" in {
    val context = writerContextWithProperties(
      "github.base_url" -> "https://github.com/lightbend/paradox")

    markdown("@github[See build.sbt](/build.sbt)")(context) shouldEqual
      html("""<p><a href="https://github.com/lightbend/paradox/tree/master/build.sbt">See build.sbt</a></p>""")
  }

  it should "throw exceptions for unconfigured GitHub URL" in {
    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@github[#1](#1)")(writerContext)
    } should have message "Failed to resolve [#1] referenced from [test.html] because property [github.base_url] is not defined"
  }

  it should "throw exceptions for invalid GitHub URLs" in {
    val invalidContext = writerContextWithProperties("github.base_url" -> "https://github.com/project")

    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@github[#1](#1)")(invalidContext)
    } should have message "Failed to resolve [#1] referenced from [test.html] because [github.base_url] is not a project URL"

    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@github[README.md](/README.md)")(invalidContext)
    } should have message "Failed to resolve [/README.md] referenced from [test.html] because [github.base_url] is not a project or versioned tree URL"
  }

  it should "throw link exceptions for invalid GitHub URL" in {
    val brokenContext = writerContextWithProperties("github.base_url" -> "https://github.com/broken/project|")

    the[ExternalLinkDirective.LinkException] thrownBy {
      markdown("@github[#1](#1)")(brokenContext)
    } should have message "Failed to resolve [#1] referenced from [test.html] because property [github.base_url] contains an invalid URL [https://github.com/broken/project|]"
  }

}
