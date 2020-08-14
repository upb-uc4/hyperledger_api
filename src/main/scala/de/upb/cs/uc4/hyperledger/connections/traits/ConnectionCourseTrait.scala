package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{HyperledgerExceptionTrait, TransactionExceptionTrait}

trait ConnectionCourseTrait extends AbstractConnectionTrait {

  /**
   * Executes the "addCourse" query.
   *
   * @param jSonCourse Information about the course to add.
   * @throws Exception if chaincode throws an exception.
   * @return Success_state
   */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  def addCourse(jSonCourse: String): String
  /**
   * Submits the "deleteCourseById" query.
   *
   * @param courseId courseId to delete course
   * @throws Exception if chaincode throws an exception.
   * @return success_state
   */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  def deleteCourseById(courseId: String): String
  /**
   * Submits the "updateCourseById" query.
   *
   * @param courseId   courseId to update course
   * @param jSonCourse courseInfo to update to
   * @throws Exception if chaincode throws an exception.
   * @return success_state
   */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  def updateCourseById(courseId: String, jSonCourse: String): String

  /**
   * Executes the "getCourses" query.
   *
   * @throws Exception if chaincode throws an exception.
   * @return List of courses represented by their json value.
   */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  def getAllCourses: String

  /**
   * Executes the "getCourseById" query.
   *
   * @param courseId courseId to get course information
   * @throws Exception if chaincode throws an exception.
   * @return JSon Course Object
   */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  def getCourseById(courseId: String): String
}