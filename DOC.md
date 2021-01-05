# Future Trouble shooting

Error response from daemon: failed to create endpoint (...) on network bridge

OR

dockerfile cant retrieve image id from build stream

=> REBOOT

# uploading to docker hub

```
docker commit <container-id> felicious/matex-backend

docker commit matex-backend-local felicious/matex-backend

docker push felicious/matex-backend
```

## updating on sever

docker pull felicious/matex-backend docker-compose restart