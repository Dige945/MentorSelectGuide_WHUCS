<template>
  <div class="task-detail-container">
    <h1 class="page-title">{{ taskName }}</h1>
    
    <el-card shadow="hover" class="info-card">
      <template #header>
        <div class="card-header">
          <span>研究方向介绍</span>
        </div>
      </template>
      <div class="task-description">
        <div class="description-en">{{ taskData.description.en }}</div>
        <div class="description-cn">{{ taskData.description.cn }}</div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'TaskDetail',
  data() {
    const descriptions = {
      'image-classification': {
        en: 'Image Classification is a fundamental task in vision recognition that aims to understand and categorize an image as a whole under a specific label. Unlike object detection, which involves classification and location of multiple objects within an image, image classification typically pertains to single-object images. When the classification becomes highly detailed or reaches instance-level, it is often referred to as image retrieval, which also involves finding similar images in a large database.',
        cn: '图像分类是视觉识别中的基础任务，旨在理解并将整个图像归类到特定标签下。与目标检测不同，图像分类通常针对单目标图像。当分类变得非常详细或达到实例级别时，通常被称为图像检索，这也涉及在大型数据库中查找相似图像。'
      },
      'object-classification': {
        en: 'Object Classification is a computer vision task that involves identifying and categorizing specific objects within an image. Unlike image classification which focuses on the entire image, object classification deals with individual objects, often requiring precise localization and recognition of multiple objects within a single image. This task is fundamental to many applications such as autonomous driving, surveillance, and robotics.',
        cn: '目标分类是计算机视觉中的一项任务，涉及识别和分类图像中的特定对象。与关注整个图像的图像分类不同，目标分类处理单个对象，通常需要在单个图像中精确定位和识别多个对象。这项任务在自动驾驶、监控和机器人等许多应用中都是基础。'
      },
      'object-detection': {
        en: 'Object Detection is a computer vision task that involves both localizing and classifying objects within an image. It goes beyond simple classification by providing the exact location of objects through bounding boxes. This technology is crucial for applications like autonomous vehicles, where it\'s essential to know not just what objects are present, but also where they are located in the environment.',
        cn: '目标检测是计算机视觉中的一项任务，涉及定位和分类图像中的对象。它通过边界框提供对象的精确位置，超越了简单的分类。这项技术对于自动驾驶等应用至关重要，因为不仅需要知道存在什么对象，还需要知道它们在环境中的位置。'
      },
      'semantic-segmentation': {
        en: 'Semantic Segmentation is a computer vision task that involves assigning a class label to each pixel in an image. Unlike object detection which uses bounding boxes, semantic segmentation provides pixel-level understanding of the scene. This detailed understanding is essential for applications like autonomous driving, medical image analysis, and augmented reality.',
        cn: '语义分割是计算机视觉中的一项任务，涉及为图像中的每个像素分配类别标签。与使用边界框的目标检测不同，语义分割提供了场景的像素级理解。这种详细的理解对于自动驾驶、医学图像分析和增强现实等应用至关重要。'
      },
      'instance-segmentation': {
        en: 'Instance Segmentation is an advanced computer vision task that combines object detection and semantic segmentation. It not only identifies and classifies objects but also distinguishes between different instances of the same class. This fine-grained understanding is particularly useful in applications like autonomous driving, where it\'s important to track individual objects separately.',
        cn: '实例分割是一项高级计算机视觉任务，结合了目标检测和语义分割。它不仅识别和分类对象，还区分同一类别的不同实例。这种细粒度的理解在自动驾驶等应用中特别有用，因为需要单独跟踪各个对象。'
      },
      'panoptic-segmentation': {
        en: 'Panoptic Segmentation is a comprehensive computer vision task that unifies semantic segmentation and instance segmentation. It aims to provide a complete understanding of a scene by classifying every pixel while also distinguishing between different instances of the same class. This holistic approach is valuable for applications requiring complete scene understanding.',
        cn: '全景分割是一项全面的计算机视觉任务，统一了语义分割和实例分割。它旨在通过对每个像素进行分类，同时区分同一类别的不同实例，提供场景的完整理解。这种整体方法对于需要完整场景理解的应用很有价值。'
      },
      'image-retrieval': {
        en: 'Image Retrieval is a computer vision task that focuses on finding similar images from a large database based on a query image. It goes beyond simple classification by considering visual similarity and semantic meaning. This technology is widely used in applications like e-commerce, where users can search for similar products using images.',
        cn: '图像检索是一项计算机视觉任务，专注于基于查询图像从大型数据库中查找相似图像。它通过考虑视觉相似性和语义含义，超越了简单的分类。这项技术广泛应用于电子商务等应用，用户可以使用图像搜索相似产品。'
      },
      'face-recognition': {
        en: 'Face Recognition is a specialized computer vision task that focuses on identifying or verifying individuals from facial images. It involves various sub-tasks such as face detection, alignment, and feature extraction. This technology has widespread applications in security systems, access control, and social media platforms.',
        cn: '人脸识别是一项专门的计算机视觉任务，专注于从面部图像中识别或验证个人。它涉及人脸检测、对齐和特征提取等多个子任务。这项技术在安全系统、访问控制和社会媒体平台中有广泛应用。'
      },
      'pose-estimation': {
        en: 'Pose Estimation is a computer vision task that involves determining the position and orientation of objects, particularly human bodies, in images or videos. It plays a crucial role in applications like motion capture, augmented reality, and human-computer interaction.',
        cn: '姿态估计是一项计算机视觉任务，涉及确定图像或视频中物体（特别是人体）的位置和方向。它在动作捕捉、增强现实和人机交互等应用中起着关键作用。'
      },
      'action-recognition': {
        en: 'Action Recognition is a computer vision task that focuses on identifying and classifying human actions in videos. It involves understanding temporal patterns and motion features to recognize activities like walking, running, or more complex actions. This technology is essential for video surveillance, sports analysis, and human-computer interaction.',
        cn: '动作识别是一项计算机视觉任务，专注于识别和分类视频中的人类动作。它涉及理解时间模式和运动特征，以识别行走、跑步或更复杂的动作。这项技术对于视频监控、运动分析和人机交互至关重要。'
      },
      'video-understanding': {
        en: 'Video Understanding is a comprehensive computer vision task that involves analyzing and interpreting video content. It includes various sub-tasks such as action recognition, object tracking, and scene understanding. This technology is fundamental for applications like video surveillance, content analysis, and autonomous systems.',
        cn: '视频理解是一项全面的计算机视觉任务，涉及分析和解释视频内容。它包括动作识别、目标跟踪和场景理解等多个子任务。这项技术对于视频监控、内容分析和自主系统等应用至关重要。'
      },
      'default': {
        en: 'This is a sample research direction description. In practical applications, this should contain specific introduction, research background, main challenges, and application scenarios of the research direction.',
        cn: '这是一个示例研究方向描述。在实际应用中，这里应该包含该研究方向的具体介绍、研究背景、主要挑战、应用场景等信息。'
      }
    }

    return {
      taskName: this.$route.params.task,
      taskData: {
        description: descriptions[this.$route.params.task] || descriptions.default
      }
    }
  }
}
</script>

<style scoped>
.task-detail-container {
  padding: 20px;
}

.page-title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-description {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
}

.description-en {
  margin-bottom: 20px;
}

.description-cn {
  color: #909399;
  font-size: 14px;
}
</style> 