const fs=require('fs'),path=require('path'),vm=require('vm');
const root=path.resolve(__dirname,'..');let scripts=0,requests=0;
function visit(items){for(const item of items){if(item.item)visit(item.item);if(item.request)requests++;for(const event of item.event||[]){new vm.Script(event.script.exec.join('\n'),{filename:item.name+':'+event.listen});scripts++;}}}
for(const f of fs.readdirSync(path.join(root,'postman')).filter(f=>f.endsWith('postman_collection.json'))){const c=JSON.parse(fs.readFileSync(path.join(root,'postman',f),'utf8'));visit(c.item);}
const result={scope:'Postman JavaScript syntax only; requests have not run against a backend',requests,scripts,passed:true};
fs.writeFileSync(path.join(root,'qa','postman-syntax-check.json'),JSON.stringify(result,null,2));
process.stdout.write(JSON.stringify(result));
