# CustomLang Compiler Frontend - Phase 2: Syntax Analyzer (LL(1) Parser)

This repository contains the Phase 2 implementation of a custom compiler built from scratch in Java. It features an automated LL(1) Predictive Parser that handles grammar transformations, FIRST/FOLLOW set calculations, predictive table construction, stack-based parsing, and parse tree generation.

## Team Members
* **Muhammad Arslan** (Roll No: 23i-0572)
* **Masab Tahir** (Roll No: 23i-0006)

## Programming Language Used
* **Java** (JDK 8 or higher)

---

## Compilation Instructions
-> Run javac src/*.java in the root folder
-> Then run java src.Main

## Known Limitations: LL(1) FIRST/FIRST Conflicts Post-Transformation
Our GrammarTransformer successfully removes indirect left recursion based on standard substitution algorithms (demonstrated in Grammar 4). However, as seen with the string b d a, removing indirect recursion can sometimes expose underlying FIRST/FIRST conflicts (e.g., both Start -> b and Start -> Alpha a can begin with b). Because our parser is strictly $LL(1)$, it can only retain one production per table cell, leading to deterministic failure on strings that require the overwritten path. This highlights a fundamental theoretical limitation of 1-lookahead predictive parsing on heavily factored grammars.