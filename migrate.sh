if [[ "$1" == "all" ]]; then
  for d in services/*; do
    service_name=$(basename "$d")
    mvn flyway:migrate@$service_name -P $2
  done
else
  mvn flyway:migrate@$1 -P $2
fi