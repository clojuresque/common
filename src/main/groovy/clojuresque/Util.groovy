/*-
 * Copyright 2012,2013 © Meikel Brandmeyer.
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package clojuresque

import org.slf4j.Logger

import us.bpsm.edn.Keyword
import us.bpsm.edn.printer.Printers

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.SequenceInputStream
import java.io.StringWriter
import java.net.URL
import java.util.Properties

class Util {
    static Properties properties(plugin) {
        def props = new Properties()

        Util.class.
            getResourceAsStream("${plugin}.properties").
            withReader("UTF-8") { props.load it }

        return props
    }

    static deprecationWarning(Logger l, String o, String n) {
        l.warn(String.format("'%s' is deprecated and will go away in a future version. Please use '%s' instead.", o, n))
    }

    static camelCaseToSnakeCase(String camelCase) {
        Keyword.newKeyword(
            camelCase.replaceAll("([A-Z])", "-\$1").toLowerCase()
        )
    }

    static camelCaseToSnakeCase(Map camelCase) {
        camelCase.collectEntries { k, v ->
            [ camelCaseToSnakeCase(k),
              (v instanceof Map) ? camelCaseToSnakeCase(v) : v ]
        }
    }

    static optionsToStream(options) {
        def outWriter  = new StringWriter()
        def ednPrinter = Printers.newPrinter(outWriter)
        ednPrinter.printValue(camelCaseToSnakeCase(options))

        new ByteArrayInputStream(outWriter.toString().getBytes("UTF-8"))
    }

    static relativizePath(baseDir, absolutePath) {
        def baseDirS      = baseDir.path
        def absolutePathS = absolutePath.path

        (absolutePathS.startsWith(baseDirS)) ?
            absolutePathS.substring(baseDirS.length() + 1) :
            null
    }

    static toInputStream(InputStream stream) {
        return stream
    }

    static toInputStream(String fileName) {
        return new ByteArrayInputStream(fileName.getBytes("UTF-8"))
    }

    static toInputStream(File file) {
        return new FileInputStream(file)
    }

    static toInputStream(URL resource) {
        if ("file" == resource.protocol)
            return toInputStream(new File(resource.file))
        else
            return resource.openStream()
    }

    static toInputStream(List l) {
        return l.reverse().collect { toInputStream(it) }.inject { t, h ->
            new SequenceInputStream(h, t)
        }
    }
}