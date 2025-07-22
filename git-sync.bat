@echo off
REM 自動同步 Git 變更的批次檔

REM 顯示目前 Git 狀態
git status

REM 把所有變更加入暫存區
git add .

REM 提示輸入 commit 訊息
set /p commitmsg=請輸入此次提交的訊息: 

REM commit
git commit -m "%commitmsg%"

REM push 到遠端 main 分支
git push origin main

pause
