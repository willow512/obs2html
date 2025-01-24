# Obs2Html
Obsidian (Markdown) to Html converter.

This tool converts a subfolder of your obsidian vault to site ready html.


# Calling:
```terminal
obs2htmlgraal
  --title "This is my site title"
  -i="Source path"
  -o="Target path"
  -t=template.html
```
--title: Your site title (Default: Obs 2 Html)
-i: Your site's source path, aka, the obsidian subfolder.
-o: Target path, restricted to filesystem for now Default: site in current folder
-t: the template - "default value = template.html"
# Menu
Your site should have a menu.md file on it
# Graal VM
In order to fix the graal cannot find reflection stuff issues:
```
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -cp build/libs/obs2htmlgraal-all.jar willow.obs2html.MainKt --title site -i=siteinput -o=site -t=template.html
```
Then run with:
```bash
./build/native/nativeCompile/obs2htmlgraal.exe --title site -i=siteinput -o=site -t=template.html
```
