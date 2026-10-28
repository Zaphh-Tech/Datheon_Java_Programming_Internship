#!/bin/sh
# Compiles all four tasks. Each task ends up in its own out/ folder.
set -e

for task in task1-grade-manager task2-atm-banking task3-word-analyzer task4-library-system; do
    printf '%s: ' "$task"
    javac -d "$task/out" "$task"/src/*/*.java
    echo "compiled"
done

echo
echo "Run a task from inside its folder, for example:"
echo "  cd task1-grade-manager && java -cp out grades.GradeManager ../data/sample_student_grades.csv"
