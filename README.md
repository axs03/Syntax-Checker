# Syntax Checker
Made by Aman Sahu
## Test Cases
### Fail Cases
| Test Case      | Status |
|----------------|--------|
| fail_01        | Y      |
| fail_02        | Y      |
| fail_03 (a, b) | Y      |
| fail_04        | Y      |
| fail_05 (a, b) | Y      |
| fail_06        | Y      |
| fail_07        | Y      |
| fail_08 (a, b) | Y      |
| fail_09        | Y      |
| fail_10        | Y      |

### Success Cases
| Test Case | Status |
|-----------|--------|
| succ_01   | Y      |
| succ_02   | Y      |
| succ_03   | Y      |
| succ_04   | Y      |
| succ_05   | Y      |
| succ_06   | Y      |
| succ_07   | Y      |
| succ_08   | Y      |
| succ_09   | Y      |
| succ_10   | Y      |
|           |        |

## Compile flex using JFlex binary
``` bash
  cd src
  java -jar jflex-1.6.1.jar Lexer.flex
```