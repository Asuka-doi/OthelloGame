# 使用するベースイメージを指定（Tomcat + OpenJDK）
FROM tomcat:10-jdk21

# 作成したアプリケーションのコードをコンテナにコピー
# `src/main/webapp`をTomcatの`webapps/othello_game`にコピー
COPY src/main/webapp/game /usr/local/tomcat/webapps/othello_game/game
COPY src/main/webapp/WEB-INF /usr/local/tomcat/webapps/othello_game/WEB-INF
COPY src/main/webapp/META-INF /usr/local/tomcat/webapps/othello_game/META-INF
COPY build/classes /usr/local/tomcat/webapps/othello_game/WEB-INF/classes

# 必要に応じて、JSPやサーブレットクラスを`WEB-INF/classes`に追加
#COPY src/main/java /usr/local/tomcat/webapps/WEB-INF/classes/

# 必要に応じて、JSPやサーブレットクラスを`WEB-INF/classes`に追加
#COPY src/main/java /usr/local/tomcat/webapps/WEB-INF/classes/


# Tomcatのポート8080を開放
EXPOSE 8080

# Tomcatを起動
CMD ["catalina.sh", "run"]
