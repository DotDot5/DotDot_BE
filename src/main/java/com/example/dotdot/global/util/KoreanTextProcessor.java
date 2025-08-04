package com.example.dotdot.global.util;

import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.collection.Seq;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KoreanTextProcessor {
    public List<String> extractNouns(String text) {
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);

        Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

        List<KoreanTokenizer.KoreanToken> tokenList = scala.collection.JavaConverters.seqAsJavaList(tokens);
        return tokenList.stream()
                .filter(token -> token.pos().toString().equals("Noun"))
                .map(token -> token.text())
                .map(CharSequence::toString)
                .distinct()
                .collect(Collectors.toList());
    }
}
