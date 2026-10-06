const { onDocumentDeleted, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { defineSecret, defineString } = require("firebase-functions/params");
const cloudinary = require("cloudinary").v2;

const cloudName = defineString("CLOUDINARY_CLOUD_NAME");
const apiKey = defineSecret("CLOUDINARY_API_KEY");
const apiSecret = defineSecret("CLOUDINARY_API_SECRET");

const functionOptions = {
  document: "users/{userId}",
  region: "southamerica-east1",
  secrets: [apiKey, apiSecret],
};

function configureCloudinary() {
  cloudinary.config({
    cloud_name: cloudName.value(),
    api_key: apiKey.value(),
    api_secret: apiSecret.value(),
    secure: true,
  });
}

async function deleteOwnedProfileImage(publicId, userId) {
  if (!publicId || !publicId.startsWith(`spotly/profiles/${userId}/`)) {
    return;
  }

  configureCloudinary();
  await cloudinary.uploader.destroy(publicId, {
    resource_type: "image",
    invalidate: true,
  });
}

exports.deleteReplacedProfileImage = onDocumentUpdated(
  functionOptions,
  async (event) => {
    const previousPublicId = event.data.before.data().profileImagePublicId || "";
    const currentPublicId = event.data.after.data().profileImagePublicId || "";

    if (previousPublicId !== currentPublicId) {
      await deleteOwnedProfileImage(previousPublicId, event.params.userId);
    }
  }
);

exports.deleteProfileImageWithUser = onDocumentDeleted(
  functionOptions,
  async (event) => {
    const publicId = event.data.data().profileImagePublicId || "";
    await deleteOwnedProfileImage(publicId, event.params.userId);
  }
);
