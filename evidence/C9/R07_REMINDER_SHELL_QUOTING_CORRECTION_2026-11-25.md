# R07 reminder shell quoting self-review correction

Base 87a9aae65b8406cc716b473dbbf57283a98da302, shell blob e34f18900f1bde022c728ed7b478af47e5c1e7d0. Source review identified literal backticks inside double-quoted shell SQL: Bash would execute command substitution before sending the query. Replace only SQL quote construction with MySQL CHAR(96). Same column projection and exact row oracle, no runtime changes. Observed execution failure NOT_RUN at correction time; source defect only. CI pending.
