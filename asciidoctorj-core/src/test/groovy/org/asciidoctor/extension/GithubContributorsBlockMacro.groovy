package org.asciidoctor.extension

import groovy.json.JsonSlurper
import org.asciidoctor.ast.AbstractBlock
import org.asciidoctor.ast.Column
import org.asciidoctor.ast.Row
import org.asciidoctor.ast.Table
import org.asciidoctor.util.TestHttpServer

class GithubContributorsBlockMacro extends BlockMacroProcessor {

    GithubContributorsBlockMacro(String macroName) {
        super(macroName)
    }

    @Override
    Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        URL url = new URL("http://api.github.com/repos/${target}/contributors")
        URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress('localhost', TestHttpServer.instance.localPort)))
        String content = connection.inputStream.text
        def contributors = new JsonSlurper().parseText(content)

        // Create the table
        Table table = createTable(parent)
        table.grid = 'rows'
        table.title = "Github contributors of $target"

        // Create the columns 'Login' and 'Contributions'
        Column loginColumn = createTableColumn(table, 0)
        Column contributionsColumn = createTableColumn(table, 1)
        contributionsColumn.setHorizontalAlignment(Table.HorizontalAlignment.CENTER)

        // Create the single header row with the column titles
        Row headerRow = createTableRow(table)
        headerRow.cells << createTableCell(loginColumn, 'Login')
        headerRow.cells << createTableCell(loginColumn, 'Contributions')
        table.header << headerRow

        // for each contributor create a cell with the login and the number of contributions
        contributors.each { contributor ->
            Row row = createTableRow(table)
            row.cells << createTableCell(loginColumn, contributor.login)
            row.cells << createTableCell(contributionsColumn, contributor.contributions as String)
            table.body << row
        }
        table
    }
}
