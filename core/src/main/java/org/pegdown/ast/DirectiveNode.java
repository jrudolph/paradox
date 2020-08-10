/*
 * Copyright © 2015 - 2019 Lightbend, Inc. <http://www.lightbend.com>
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

package org.pegdown.ast;

import java.util.List;
import org.parboiled.common.ImmutableList;
import org.parboiled.common.StringUtils;

/**
 * General node for directives.
 */
public class DirectiveNode extends AbstractNode {

    public enum Format { Inline, LeafBlock, ContainerBlock }

    public final Format format;
    public final String name;
    public final String label;
    public final Source source;
    public final DirectiveAttributes attributes;
    public final String contents;
    public final Node contentsNode;

    // Workaround for bug in ScalaDoc for Scala 2.12.x https://github.com/scala/bug/issues/10509
    public DirectiveNode(DirectiveNode.Format format, String name, String label, DirectiveNode.Source source, DirectiveAttributes attributes, Node labelNode) {
        this(format, name, label, source, attributes, label, labelNode);
    }

    // Workaround for bug in ScalaDoc for Scala 2.12.x https://github.com/scala/bug/issues/10509
    public DirectiveNode(DirectiveNode.Format format, String name, String label, DirectiveNode.Source source, DirectiveAttributes attributes, String contents, Node contentsNode) {
        this.format = format;
        this.name = name;
        this.label = label;
        this.source = source;
        this.attributes = attributes;
        this.contents = contents;
        this.contentsNode = contentsNode;
    }

    public List<Node> getChildren() {
        return ImmutableList.of(contentsNode);
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" " + format.name() + " '" + name + "'");
        if (!label.isEmpty()) {
            sb.append(" [" + StringUtils.escape(label) + "]");
        }
        source.format(sb);
        if (!attributes.isEmpty()) {
            sb.append(" " + attributes.toString());
        }
        return sb.toString();
    }

    /**
     * Poor man's ADT ...
     */
    public static abstract class Source {
        public abstract void format(StringBuilder sb);

        public static final class Direct extends Source {
            public final String value;
            public Direct(String value) { this.value = value; }
            public void format(StringBuilder sb) { sb.append('(').append(StringUtils.escape(value)).append(')'); }

            @Override public String toString() { return "Source.Direct("+value+")"; }
        }

        public static final class Ref extends Source {
            public final String value;
            public Ref(String value) { this.value = value; }
            public void format(StringBuilder sb) { sb.append('[').append(value).append(']'); }

            @Override public String toString() { return "Source.Ref("+value+")"; }
        }

        public static final Source Empty = new Source() {
            public void format(StringBuilder sb) { }
            @Override public String toString() { return "Source.EMPTY"; }
        };
    }
}
