package gainnim.fight.service
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.Image
import java.util.UUID

@Service
class S3UploadService(@Value("\${cloud.aws.s3.bucket}") val bucket: String, val amazonS3: AmazonS3) {
    val log = LoggerFactory.getLogger(javaClass)
    fun saveImage(image: MultipartFile, directory: String): String {
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size
        metadata.contentType = image.contentType

        val uuid = UUID.randomUUID()

        val key = "${directory}/${uuid}"

        image.inputStream.use { amazonS3.putObject(bucket, key, it, metadata) }

        return amazonS3.getUrl(bucket, key).toString()
    }
}