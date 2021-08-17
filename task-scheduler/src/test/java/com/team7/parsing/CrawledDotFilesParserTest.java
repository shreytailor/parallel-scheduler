package com.team7.parsing;

import com.team7.model.Graph;
import com.team7.testutil.GraphInfoUtil;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;


class CrawledDotFilesParserTest {
    @Test
    void dotFilesParse() throws FileNotFoundException {
        String fileName ="src/dot-tests/asdf.dot";
        Graph graph1 = DOTParser.read(fileName);
        GraphInfoUtil.GraphInfo graphInfo = GraphInfoUtil.getGraphInfo("src/dot-tests/asdf.dot");

//        Graph graph2 = DOTParser.read("src/dot-tests/Nodes_7_OutTree.dot");
//        System.out.println(graph1.getEdges());
//        System.out.println(graph2.getEdges());

    }



}