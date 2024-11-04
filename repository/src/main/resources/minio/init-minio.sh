mc alias set minio http://minio:9000 nikita 12345678

mc mb minio/book-covers
mc anonymous set public minio/book-covers

mc mb minio/user-avatars
mc anonymous set public minio/user-avatars

mc mb minio/chapters-content
mc anonymous set public minio/chapters-content

mc mb minio/chapters-media
mc anonymous set public minio/chapters-media

mc mb minio/comments-content
mc anonymous set public minio/comments-content

mc mb minio/comments-media
mc anonymous set public minio/comments-media

mc mb minio/reviews-content
mc anonymous set public minio/reviews-content

mc mb minio/reviews-media
mc anonymous set public minio/reviews-media
